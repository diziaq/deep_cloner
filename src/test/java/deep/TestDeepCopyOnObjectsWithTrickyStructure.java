package deep;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Objects;

import static deep.Util.assertDistinctInstancesMatch;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestDeepCopyOnObjectsWithTrickyStructure {

    @Test
    void should_fail_when_ctor_check_nulls_and_is_not_canonical() {
        var original = new TrickyObjectA(new Object());
        assertThatThrownBy(() -> DeepCopyMaker.deepCopy(original))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessageStartingWith("Unable to make deep copy of ")
            .hasMessageContaining("$TrickyObjectA@")
            .hasCauseInstanceOf(NoSuchMethodException.class);
    }

    @Test
    void should_fail_when_canonical_ctor_absent_and_minimal_args_ctor_throws() {
        var original = new TrickyObjectC(0, 0);

        assertThatThrownBy(() -> DeepCopyMaker.deepCopy(original))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessageStartingWith("Unable to make deep copy of ")
            .hasMessageContaining("$TrickyObjectC@")
            .hasStackTraceContaining("java.lang.IllegalArgumentException: intentional error");
    }

    @Test
    void should_copy_object_when_single_canonical_ctor() {
        var original = new TrickyObjectB("dvs", new Date());
        var copy = DeepCopyMaker.deepCopy(original);

        assertDistinctInstancesMatch(copy, original);
    }

    @Test
    void should_copy_object_using_canonical_ctor_when_default_ctor_throws_npe() {
        var original = new TrickyObjectD(1L);
        var copy = DeepCopyMaker.deepCopy(original);

        assertDistinctInstancesMatch(copy, original);
    }

    static class TrickyObjectA {
        private final int first;
        private final Object second;

        TrickyObjectA(Object second) {
            Objects.requireNonNull(second, "hello");

            this.first = 123;
            this.second = second;
        }
    }

    static class TrickyObjectB {
        private final String first;
        private final Date second;

        TrickyObjectB(String first, Date second) {
            Objects.requireNonNull(first, "first");
            Objects.requireNonNull(second, "second");
            this.first = first;
            this.second = second;
        }
    }

    static class TrickyObjectC {
        private final int first;

        TrickyObjectC(int first) {
            throw new IllegalArgumentException("intentional error");
        }

        TrickyObjectC(int secret, int instance) {
            this.first = 123;
        }
    }

    static class TrickyObjectD {
        private final Long first;

        TrickyObjectD() {
            throw new NullPointerException("intentional error");
        }

        TrickyObjectD(Long first) {
            this.first = first;
        }
    }
}
