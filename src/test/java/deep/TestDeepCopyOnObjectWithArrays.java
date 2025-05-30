package deep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static deep.Util.assertDistinctInstancesMatch;

class TestDeepCopyOnObjectWithArrays {

    @Test
    void should_copy_object_when_all_fields_are_arrays() {
        var original = new ArrayFieldsHolder();
        var copy = DeepCopyMaker.deepCopy(original);

        assertDistinctInstancesMatch(copy, original);
    }

    @ParameterizedTest
    @MethodSource("arraySamples")
    void should_copy_standalone_array(Handle originalHandle) {
        var original = originalHandle.array;
        var copy = DeepCopyMaker.deepCopy(original);

        assertDistinctInstancesMatch(copy, original);
    }

    static Stream<Object> arraySamples() {
        return Stream.of(
            new boolean[]{true, false},
            new byte[]{1, 2, 3},
            new short[]{10, 20},
            new int[]{100, 200, 300},
            new long[]{1L, 2L, 3L},
            new float[]{1.1F, 2.2F},
            new double[]{3.3, 4.4},
            new char[]{'a', 'b', 'c'},

            new Boolean[]{Boolean.TRUE, Boolean.FALSE},
            new Byte[]{10, 20},
            new Short[]{-30, 40, -50, 60},
            new Integer[]{50, -60, 5438, -654930, 435634},
            new Long[]{2343250L, -835462110L, 234535L},
            new Float[]{90.982357F, 104223.756431F, 82957459329F, Float.MAX_VALUE, Float.MIN_VALUE, Float.NaN},
            new Double[]{11.1474234D, 235235.3452342D, 0D, Double.NaN, -12145D},
            new Character[]{'X', 'Y'},

            new String[]{"etahetqyu4", "46jhnwbeqghj4"},
            new SampleEnum[]{SampleEnum.ANNA, SampleEnum.CAROL, SampleEnum.BEN}
        ).map(Handle::new);
    }

    // helper for parameterized test arguments to prevent array unfolding
    record Handle(Object array) {}

    static class ArrayFieldsHolder {
        private final boolean[] booleans = {true, false};
        private final byte[] bytes = {1, 2, 3};
        private final short[] shorts = {10, 20};
        private final int[] ints = {100, 200, 300};
        private final long[] longs = {1L, 2L, 3L};
        private final float[] floats = {1.1F, 2.2F};
        private final double[] doubles = {3.3, 4.4};
        private final char[] chars = {'a', 'b', 'c'};

        private final Boolean[] booleanWrappers = {Boolean.TRUE, Boolean.FALSE};
        private final Byte[] byteWrappers = {10, 20};
        private final Short[] shortWrappers = {-30, 40, -50, 60};
        private final Integer[] intWrappers = {50, -60, 5438, -654930, 435634};
        private final Long[] longWrappers = {2343250L, -835462110L, 234535L};
        private final Float[] floatWrappers = {90.982357F, 104223.756431F, 82957459329F, Float.MAX_VALUE, Float.MIN_VALUE, Float.NaN};
        private final Double[] doubleWrappers = {11.1474234D, 235235.3452342D, 0D, Double.NaN, -12145D};
        private final Character[] charWrappers = {'X', 'Y'};

        private final String[] strings = {"etahetqyu4", "46jhnwbeqghj4"};
        private final SampleEnum[] enums = {SampleEnum.ANNA, SampleEnum.CAROL, SampleEnum.BEN};
    }

    enum SampleEnum {
        ANNA, BEN, CAROL
    }
}
