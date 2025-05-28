package deep;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TestDeepCopyOnAtomicTypes {

    @ParameterizedTest
    @MethodSource("atomicValues")
    void should_return_same_instance_when_atomic_type(Object original) {
        Object copy = DeepCopyMaker.deepCopy(original);
        assertThat(copy).isSameAs(original);
    }

    static Stream<Object> atomicValues() {
        return Stream.of(
            "hello",
            Boolean.TRUE,
            Boolean.FALSE,
            (byte) 134,
            (short) 45343,
            547326343,
            -2345643824734L,
            3657545.4567455F,
            4636456734.6654732345D,
            'A',
            SampleEnum.ITEM1,
            SampleEnum.ITEM2,
            SampleEnum.ITEM3
        );
    }

    enum SampleEnum {
        ITEM1, ITEM2, ITEM3
    }
}
