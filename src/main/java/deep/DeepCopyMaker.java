package deep;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

    public T make() throws Exception {
        Class<?> clazz = original.getClass();

        if (typesExpert.isAtomic(clazz)) {
            return original;
        }

        Object copy = createInstance(clazz);
        for (Field field : getAllFields(clazz)) {
            field.setAccessible(true);
            Object value = field.get(original);

            if (value == null || typesExpert.isAtomic(field.getType())) {
                field.set(copy, value);
            } else {
                throw new UnsupportedOperationException("Nested objects not supported");
            }
        }

        return (T) copy;
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
