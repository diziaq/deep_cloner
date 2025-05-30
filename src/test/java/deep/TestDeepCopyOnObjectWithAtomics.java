package deep;

import org.junit.jupiter.api.Test;

import static deep.Util.assertDistinctInstancesMatch;
import static org.assertj.core.api.Assertions.assertThat;

class TestDeepCopyOnObjectWithAtomics {

    @Test
    void should_return_null_when_original_is_null() {
        var copy = DeepCopyMaker.deepCopy(null);
        assertThat(copy).isNull();
    }

    @Test
    void should_copy_object_when_all_fields_atomic() {
        var original = new AtomicFieldsOnly();
        var copy = DeepCopyMaker.deepCopy(original);

        assertDistinctInstancesMatch(copy, original);
    }

    @Test
    void should_copy_object_when_no_fields() {
        var original = new NoFields();
        var copy = DeepCopyMaker.deepCopy(original);

        assertDistinctInstancesMatch(copy, original);
    }

    static class NoFields {
    }

    static class AtomicFieldsOnly {
        private final boolean booleanFi = true;
        private final byte byteFi = 1;
        private final short shortFi = 2;
        private final int intFi = 3;
        private final long longFi = 4L;
        private final float floatFi = 5.5F;
        private final double doubleFi = 6.6;
        private final char charFi = 'H';

        private final Boolean booleanWrapFi = false;
        private final Byte byteWrapFi = 10;
        private final Short shortWrapFi = 20;
        private final Integer intWrapFi = 30;
        private final Long longWrapFi = 40L;
        private final Float floatWrapFi = 50.5F;
        private final Double doubleWrapFi = 60.6;
        private final Character charWrapFi = 'P';

        private final String stringFi = "immutable";
        private final SampleEnum enumFi = SampleEnum.A;
    }

    enum SampleEnum {
        A, B
    }
}
