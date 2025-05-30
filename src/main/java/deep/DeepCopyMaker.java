package deep;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DeepCopyMaker {

    // guard against excessive recursion
    private static final int MAX_OBJECT_GRAPH_SIZE = 3000;

    private final TypesExpert typesExpert;
    private final InstanceCrafter instanceCrafter = new InstanceCrafter();
    private final Map<Object, Object> visited = new IdentityHashMap<>();

    private int currentItemsCount = 0;

    private DeepCopyMaker() {
        this.typesExpert = new TypesExpert();
    }

    public static <T> T deepCopy(T original) {
        try {
            return original == null ? null : new DeepCopyMaker().makeCopyRecursive(original);
        } catch (Exception e) {
            throw new RuntimeException("Unable to make deep copy of %s".formatted(original), e);
        }
    }

    private <T> T makeCopyRecursive(T original) throws Exception {
        if (currentItemsCount++ > MAX_OBJECT_GRAPH_SIZE) {
            throw new IllegalStateException("Maximum copy size (%s) exceeded. Context: %s.".formatted(MAX_OBJECT_GRAPH_SIZE, original));
        }

        if (original == null) {
            return null;
        }

        Class<?> clazz = original.getClass();

        Object result;

        if (typesExpert.isAtomic(clazz)) {
            result = original;
        } else if (visited.containsKey(original)) {
            result = visited.get(original);
        } else if (clazz.isArray()) {
            result = copyArray(original, clazz.getComponentType());
        } else if (clazz.isRecord()) {
            result = copyRecord(original, clazz);
        } else {
            result = copyPlainObject(original, clazz);
        }

        @SuppressWarnings("unchecked")
        T typedResult = (T) result;

        return typedResult;
    }

    private Object copyArray(Object host, Class<?> componentClazz) throws Exception {
        int length = Array.getLength(host);
        Object hostCopy = Array.newInstance(componentClazz, length);
        visited.put(host, hostCopy);

        for (int i = 0; i < length; i++) {
            Object element = Array.get(host, i);
            Object elementCopy = makeCopyRecursive(element);
            Array.set(hostCopy, i, elementCopy);
        }

        return hostCopy;
    }

    private Object copyPlainObject(Object host, Class<?> clazz) throws Exception {
        Object hostCopy = instanceCrafter.createInstanceOf(clazz);
        visited.put(host, hostCopy);

        for (Field field : getAllFields(clazz)) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            Object value = field.get(host);
            Object valueCopy = makeCopyRecursive(value);
            field.set(hostCopy, valueCopy);
        }

        return hostCopy;
    }

    private Object copyRecord(Object record, Class<?> clazz) throws Exception {
        RecordComponent[] components = clazz.getRecordComponents();
        Object[] args = new Object[components.length];

        for (int i = 0; i < components.length; i++) {
            Object value = components[i].getAccessor().invoke(record);
            Object valueCopy = makeCopyRecursive(value);

            args[i] = valueCopy;
        }

        Constructor<?> canonicalCtor = clazz.getDeclaredConstructor(
            Stream.of(components).map(RecordComponent::getType).toArray(Class[]::new)
        );
        canonicalCtor.setAccessible(true);
        Object result = canonicalCtor.newInstance(args);
        visited.put(record, result);

        return result;
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        var fields = new ArrayList<Field>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            fields.addAll(List.of(c.getDeclaredFields()));
        }
        return fields;
    }
}
