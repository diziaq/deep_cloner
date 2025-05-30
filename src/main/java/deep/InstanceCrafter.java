package deep;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

final class InstanceCrafter {

    private final Map<Class<?>, Object> primitiveDefaults = new HashMap<>();
    private final Map<Class<?>, MakeBareInstance<?>> cache = new HashMap<>();

    public Object createInstanceOf(Class<?> clazz) {
        var maker = cache.get(clazz);

        if (maker == null) {
            var ctor =
                Stream.of(clazz.getDeclaredConstructors())
                      .min(Comparator.comparingInt(Constructor::getParameterCount))
                      .orElseThrow(() -> new IllegalStateException("No constructors found for class: " + clazz.getName()));

            ctor.setAccessible(true);
            maker = new MakeBareInstance<>(ctor, parametersCompatibleWith(ctor));
            cache.put(clazz, maker);
        }

        try {
            return maker.get();
        } catch (Exception e) {
            throw new RuntimeException("Failed instantiation for class: " + clazz.getName(), e);
        }
    }

    private Object[] parametersCompatibleWith(Constructor<?> constructor) {
        Class<?>[] paramTypes = constructor.getParameterTypes();

        Object[] args = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = getDefaultValueFor(paramTypes[i]);
        }

        return args;
    }

    /**
     * Returns a default value suitable for the given class.
     * <ul>
     *   <li>For primitive types, returns the platform-generated zero-equivalent default (via single-element array creation).</li>
     *   <li>For {@code Object[]} types, returns an empty object array — used as a local workaround for constructors with vararg parameters.</li>
     *   <li>For all other types, returns {@code null}.</li>
     * </ul>
     *
     * @param clazz the class for which the default value should be returned
     * @return the default value appropriate for the given class
     */
    private Object getDefaultValueFor(Class<?> clazz) {
        Object value = null;

        if (clazz.isPrimitive()) {
            value = primitiveDefaults.get(clazz);
            if (value == null) {
                var array = Array.newInstance(clazz, 1);
                value = Array.get(array, 0);
                primitiveDefaults.put(clazz, value);
            }
        } else if (clazz.equals(Object[].class)) {
            value = new Object[0];
        }

        return value;
    }

    private record MakeBareInstance<T>(
        Constructor<T> constructor,
        Object[] parameters
    ) {
        T get() throws Exception {
            return constructor.newInstance(parameters);
        }
    }
}
