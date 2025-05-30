package deep;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static deep.Util.assertDistinctInstancesMatch;

class TestDeepCopyOnCollections {

    @ParameterizedTest
    @MethodSource("collectionValues")
    void should_create_deep_copy_of_collections(Object original) {
        Object copy = DeepCopyMaker.deepCopy(original);

        assertDistinctInstancesMatch(copy, original);
    }

    static Stream<Object> collectionValues() {
        return Stream.of(
            List.of("a", "b", "c"),
            Set.of(1, 2, 3),
            Map.of("key1", "value1", "key2", "value2"),
            new ArrayList<>(List.of(1, 2, 3)),
            new LinkedList<>(List.of("x", "y")),
            new HashSet<>(Set.of(10, 20)),
            new TreeSet<>(Set.of(5, 15, 25)),
            new HashMap<>(Map.of(1, "one", 2, "two")),
            new TreeMap<>(Map.of("a", 1, "b", 2)),
            new ConcurrentHashMap<>(Map.of("k", "v")),
            Set.of(List.of("78675", 45L, 345.4D), 15, Set.of(5, 653, 9025L)),
            List.of(1, List.of(351, Float.NaN, 4356, 654), Set.of(15L, 86D, "$"), Set.of(2435, Double.NaN, "ABC"))
        );
    }
}
