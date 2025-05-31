package deep;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Utility class for performing deep copies of objects, supporting arrays, records, and regular POJOs.
 * The copier traverses the object graph recursively, cloning each field value, while handling shared references
 * and avoiding infinite loops through cycle detection.
 *
 * <p>
 * Features:
 * <ul>
 *   <li>Supports primitive arrays and object arrays</li>
 *   <li>Handles Java records using canonical constructors</li>
 *   <li>Uses fallback instantiation strategy for non-records via reflection</li>
 *   <li>Prevents excessive recursion via a configurable object graph size limit</li>
 * </ul>
 * </p>
 *
 * <p>
 * Note: This copier may produce inconsistent or invalid copies for objects whose classes rely on
 * special internal logic or constructor tricks — for example, {@code java.util.ImmutableCollections.List12},
 * which substitutes a shared {@code EMPTY} object for absent elements. Supporting such cases would require
 * dedicated handling, so in favor of simplicity and maintainability, these edge cases are intentionally not supported.
 * </p>
 *
 * <p>
 * Typical usage:
 * {@code
 *     MyType original = ...;
 *     MyType copy = DeepCopyMaker.deepCopy(original);
 * }
 * </p>
 */
public class DeepCopyMaker {

    // guard against excessive recursion
    private static final int MAX_OBJECT_GRAPH_SIZE = 3000;

    private final TypesExpert typesExpert;
    private final BareInstancesSource bareInstancesSource = new BareInstancesSource();
    private final Map<Object, Object> visited = new IdentityHashMap<>();

    private int currentItemsCount = 0;

    private DeepCopyMaker() {
        this.typesExpert = new TypesExpert();
    }

    public static <T> T deepCopy(T original) {
        try {
            return original == null ? null : new DeepCopyMaker().makeCopyRecursive(original);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to make deep copy of %s".formatted(original), e);
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
            result = copyViaCanonicalCtor(original, clazz);
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
        Object hostCopy = switch (bareInstancesSource.newInstanceOf(clazz)) {
            case InstantiationResult.Success(Object instance) -> instance;
            case InstantiationResult.NullPointer ignored -> copyViaCanonicalCtor(host, clazz);
            case InstantiationResult.GeneralFailure(Exception exception) -> throw new RuntimeException("Failed instantiation for class: %s".formatted(clazz), exception);
        };

        visited.put(host, hostCopy);

        for (Field field : getAllDynamicFields(clazz)) {
            Object value = field.get(host);
            Object valueCopy = makeCopyRecursive(value);
            field.set(hostCopy, valueCopy);
        }

        return hostCopy;
    }

    private Object copyViaCanonicalCtor(Object host, Class<?> clazz) throws Exception {
        List<Field> fields = getAllDynamicFields(clazz);

        Constructor<?> canonicalCtor = clazz.getDeclaredConstructor(
            fields.stream().map(Field::getType).toArray(Class[]::new)
        );
        canonicalCtor.setAccessible(true);

        Object[] args = new Object[fields.size()];

        int i = 0;
        for (Field field : fields) {
            Object value = field.get(host);
            Object valueCopy = makeCopyRecursive(value);
            args[i++] = valueCopy;
        }

        return canonicalCtor.newInstance(args);
    }

    private static List<Field> getAllDynamicFields(Class<?> topClass) {
        return Stream.iterate(topClass, cl -> cl != null && cl != Object.class, (Class<?> cl) -> cl.getSuperclass())
                     .flatMap(cl -> Stream.of(cl.getDeclaredFields()))
                     .filter(field -> !Modifier.isStatic(field.getModifiers()))
                     .peek(field -> field.setAccessible(true))
                     .toList();
    }
}
