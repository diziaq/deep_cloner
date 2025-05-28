package deep;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

public class DeepCopyMaker<T> {

    private final T original;
    private final TypesExpert typesExpert;

    private DeepCopyMaker(T instance) {
        this.original = instance;
        this.typesExpert = new TypesExpert();
    }

    public static <T> T deepCopy(T original) {
        try {
            return original == null ? null : new DeepCopyMaker<>(original).make();
        } catch (Exception e) {
            throw new RuntimeException("Unable to make deep copy of %s".formatted(original), e);
        }
    }

    @SuppressWarnings("unchecked")
    private T make() throws Exception {
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

    private Object copyPlainObject(Object object, Class<?> clazz) throws Exception {
        Object copy = createInstance(clazz);
        for (Field field : getAllFields(clazz)) {
            field.setAccessible(true);
            Object value = field.get(object);

            if (value == null || typesExpert.isAtomic(field.getType())) {
                field.set(copy, value);
            } else {
                throw new UnsupportedOperationException("Nested objects not supported");
            }
        }
        return (T) copy;
    }

    private Object copyRecord(Object record, Class<?> clazz) throws Exception {
        RecordComponent[] components = clazz.getRecordComponents();
        Object[] args = new Object[components.length];

        for (int i = 0; i < components.length; i++) {
            var comp = components[i];
            Object value = comp.getAccessor().invoke(record);

            if (value == null || typesExpert.isAtomic(comp.getType())) {
                args[i] = value;
            } else {
                throw new UnsupportedOperationException("Nested objects not supported");
            }
        }

        Constructor<?> canonicalCtor = clazz.getDeclaredConstructor(
            List.of(components).stream().map(RecordComponent::getType).toArray(Class[]::new)
        );
        canonicalCtor.setAccessible(true);
        return canonicalCtor.newInstance(args);
    }

    private static Object createInstance(Class<?> clazz) throws Exception {
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        var fields = new ArrayList<Field>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            fields.addAll(List.of(c.getDeclaredFields()));
        }
        return fields;
    }
}
