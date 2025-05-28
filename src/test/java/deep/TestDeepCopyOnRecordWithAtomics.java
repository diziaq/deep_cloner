package deep;

import org.junit.jupiter.api.Test;

import static deep.Util.assertDistinctInstancesMatch;

class TestDeepCopyOnRecordWithAtomics {

    @Test
    void should_copy_record_when_all_fields_atomic() {
        var original = new AtomicFieldsOnlyRecord(
            true,
            (byte) 81,
            (short) 6572,
            46856473,
            -56747574L,
            -96758475.576545345F,
            264567363.66546323,
            'H',
            false,
            (byte) 10,
            (short) 3420,
            -36543430,
            40574567566L,
            765450.500001F,
            -567475660.678649D,
            'P',
            "8g2b3rq9unf",
            SampleEnum.B
        );

        var copy = DeepCopyMaker.deepCopy(original);

        assertDistinctInstancesMatch(copy, original);
    }

    enum SampleEnum {
        A, B
    }

    record AtomicFieldsOnlyRecord(
        boolean booleanFi,
        byte byteFi,
        short shortFi,
        int intFi,
        long longFi,
        float floatFi,
        double doubleFi,
        char charFi,

        Boolean booleanWrapFi,
        Byte byteWrapFi,
        Short shortWrapFi,
        Integer intWrapFi,
        Long longWrapFi,
        Float floatWrapFi,
        Double doubleWrapFi,
        Character charWrapFi,

        String stringFi,
        SampleEnum enumFi
    ) {}
}
