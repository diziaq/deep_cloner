package deep;

import java.util.Set;

public class TypesExpert {

    private static final Set<Class<?>> IMMUTABLE_TYPES = Set.of(
        byte.class, short.class, long.class, float.class, double.class, boolean.class,
        Byte.class, Short.class, Long.class, Float.class, Double.class, Boolean.class,
        char.class, int.class,
        Character.class, Integer.class,
        String.class
    );

    public boolean isAtomic(Class<?> clazz) {
        return clazz.isEnum() || IMMUTABLE_TYPES.contains(clazz);
    }
}
