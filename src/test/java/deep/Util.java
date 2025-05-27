package deep;

import static org.assertj.core.api.Assertions.assertThat;

public class Util {

    public static void assertDistinctInstancesMatch(Object actual, Object expected) {
        assertThat(expected)
            .isNotSameAs(actual);

        assertThat(expected)
            .usingRecursiveComparison()
            .withComparatorForType(Double::compare, Double.class)
            .withComparatorForType(Float::compare, Float.class)
            .isEqualTo(actual);
    }
}
