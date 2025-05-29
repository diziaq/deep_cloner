package deep;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class DeepCopyMaker {

    private final TypesExpert typesExpert;
    private final InstanceCrafter instanceCrafter = new InstanceCrafter();

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

    @SuppressWarnings("unchecked")
    private <T> T makeCopyRecursive(T original) throws Exception {
        if (original == null) {
            return null;
        }

        Class<?> clazz = original.getClass();

        Object result;

        if (typesExpert.isAtomic(clazz)) {
            result = original;
        } else if (clazz.isRecord()) {
            result = copyRecord(original, clazz);
        } else {
            result = copyPlainObject(original, clazz);
        }

        return (T) result;
    }

    private Object copyPlainObject(Object host, Class<?> clazz) throws Exception {
        Object hostCopy = instanceCrafter.createInstanceOf(clazz);

        for (Field field : getAllFields(clazz)) {
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
            var comp = components[i];
            Object value = comp.getAccessor().invoke(record);
            Object valueCopy = makeCopyRecursive(value);

            args[i] = valueCopy;
        }

        Constructor<?> canonicalCtor = clazz.getDeclaredConstructor(
            Stream.of(components).map(RecordComponent::getType).toArray(Class[]::new)
        );
        canonicalCtor.setAccessible(true);
        return canonicalCtor.newInstance(args);
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        var fields = new ArrayList<Field>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            fields.addAll(List.of(c.getDeclaredFields()));
        }
        return fields;
    }
}
