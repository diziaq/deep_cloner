package deep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static deep.Util.assertDistinctInstancesMatch;
import static org.assertj.core.api.Assertions.assertThat;

class TestDeepCopyOnCircularRefs {

    @Test
    void should_copy_self_referencing_plain_object() {
        class SelfRef {
            String name;
            SelfRef ref;
        }
        SelfRef original = new SelfRef();
        original.name = "node";
        original.ref = original;

        SelfRef copy = DeepCopyMaker.deepCopy(original);

        assertDistinctInstancesMatch(copy, original);
    }

    @Test
    void should_copy_mutually_referencing_plain_objects() {
        class Pair {
            Pair partner;
        }
        Pair originalA = new Pair();
        Pair b = new Pair();
        originalA.partner = b;
        b.partner = originalA;

        Pair aCopy = DeepCopyMaker.deepCopy(originalA);

        assertDistinctInstancesMatch(aCopy, originalA);
    }

    @Test
    void should_copy_self_referencing_array() {
        Object[] original = new Object[2];
        original[0] = original;
        original[1] = original;

        Object[] copy = DeepCopyMaker.deepCopy(original);

        assertThat(copy).isNotSameAs(original);
        assertThat(copy).isSameAs(copy[0]);
        assertThat(copy).isSameAs(copy[1]);
    }

    @Test
    void should_copy_self_referencing_record() {
        record Node(String name, Node next) {}

        Node node = new Node("root", null);
        Node selfRef = new Node("inner", node);
        node = new Node("root", selfRef);

        Node copy = DeepCopyMaker.deepCopy(node);

        assertDistinctInstancesMatch(copy, node);
    }

    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    @Test
    void should_copy_large_circular_chain() {
        int circleSize = 1000;
        Car original = Car.makeCircle(circleSize);
        Car copy = DeepCopyMaker.deepCopy(original);

        assertDistinctInstancesMatch(copy, original);

        Car iOriginal = original;
        Car iCopy = copy;

        for (int i = 0; i < circleSize; i++) {
            assertThat(iCopy).isNotNull().isNotSameAs(iOriginal);
            assertThat(iCopy.id).isEqualTo(iOriginal.id);

            iOriginal = iOriginal.next;
            iCopy = iCopy.next;
        }
    }

    static class Car {
        final int id;
        Car next;

        Car(int id, Car next) {
            this.id = id;
            this.next = next;
        }

        static Car makeCircle(int size) {
            Car head = new Car(0, null);
            Car tail = IntStream.rangeClosed(1, size)
                                .mapToObj(i -> new Car(i, null))
                                .reduce(head, (curr, next) -> {
                                    curr.next = next;
                                    return next;
                                });
            tail.next = head;

            return head;
        }
    }
}
