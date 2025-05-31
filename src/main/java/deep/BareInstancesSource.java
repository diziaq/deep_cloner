package deep;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

final class BareInstancesSource {

    private final static Comparator<Constructor<?>> BY_PARAMS_COUNT = Comparator.comparingInt(Constructor::getParameterCount);
    private final static Object[] VARARG_DUMMY = new Object[0];

    private final Map<Class<?>, Object> primitiveDefaults = new HashMap<>();
    private final Map<Class<?>, Generator<?>> cache = new HashMap<>();

    public InstantiationResult newInstanceOf(Class<?> clazz) {
        var maker = cache.get(clazz);

        if (maker == null) {
            var ctor =
                Stream.of(clazz.getDeclaredConstructors())
                      .min(BY_PARAMS_COUNT)
                      .orElseThrow(() -> new IllegalStateException("No constructors found for class: " + clazz.getName()));

            ctor.setAccessible(true);
            maker = new Generator<>(ctor, parametersCompatibleWith(ctor));
            cache.put(clazz, maker);
        }

        try {
            return new InstantiationResult.Success(maker.get());
        } catch (Exception exception) {
            var unwrapped = unwrap(exception, NullPointerException.class);

            if (unwrapped instanceof NullPointerException npe) {
                return new InstantiationResult.NullPointer(npe);
            } else {
                return new InstantiationResult.GeneralFailure(exception);
            }
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
            value = VARARG_DUMMY;
        }

        return value;
    }

    /**
     * Traverses the exception cause chain and returns the first exception that matches
     * any of the specified types. If no match is found, returns the original exception.
     *
     * @param exception  the exception to unwrap
     * @param targetType exception class to match against the cause chain
     * @return the first matching exception found in the cause chain, or the original exception if none match
     */
    private static Exception unwrap(Exception exception, Class<? extends Exception> targetType) {
        Throwable current = exception;
        while (current != null) {
            if (targetType.isInstance(current)) {
                return (Exception) current;
            }
            current = current.getCause();
        }
        return exception;
    }

    private record Generator<T>(
        Constructor<T> constructor,
        Object[] parameters
    ) {
        T get() throws Exception {
            return constructor.newInstance(parameters);
        }
    }
}
