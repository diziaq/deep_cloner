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
        private boolean booleanFi = true;
        private byte byteFi = 1;
        private short shortFi = 2;
        private int intFi = 3;
        private long longFi = 4L;
        private float floatFi = 5.5f;
        private double doubleFi = 6.6;
        private char charFi = 'H';

        private Boolean booleanWrapFi = false;
        private Byte byteWrapFi = 10;
        private Short shortWrapFi = 20;
        private Integer intWrapFi = 30;
        private Long longWrapFi = 40L;
        private Float floatWrapFi = 50.5f;
        private Double doubleWrapFi = 60.6;
        private Character charWrapFi = 'P';

        private String stringFi = "immutable";
        private SampleEnum enumFi = SampleEnum.A;
    }

    enum SampleEnum {
        A, B
    }
}
