package deep;

import org.junit.jupiter.api.Test;

import java.util.List;

import static deep.Util.assertDistinctInstancesMatch;

class TestDeepCopyOnObjectWithNestedObject {

    @Test
    void should_copy_object_with_nested_object_fields() {
        var original = new WrapObject(
            new SampleObjectDefaultCtor(),
            new SampleRecord(345624, 44657343L, "ewqrgqerbhb", SampleEnum.B),
            new SampleObjectParamCtor(4539876, 5487325662L, "dfghrtt", SampleEnum.C),
            new WrapObject(
                new SampleObjectDefaultCtor(),
                new SampleRecord(-57345, 3443L, "trwhq34g3", SampleEnum.A),
                new SampleObjectParamCtor(5434326, 12345L, "htrdseaw", SampleEnum.B),
                null)
        );

        var copy = DeepCopyMaker.deepCopy(original);

        assertDistinctInstancesMatch(copy, original);
    }

    @Test
    void should_copy_basic_object_with_nested_fields() {
        var original = new Man("12", 34, List.of("1", "4"));

        var copy = DeepCopyMaker.deepCopy(original);

        assertDistinctInstancesMatch(copy, original);
    }

    record WrapObject(
        SampleObjectDefaultCtor first,
        SampleRecord second,
        SampleObjectParamCtor third,
        WrapObject fourth
    ) {}

    static class SampleObjectDefaultCtor {
        private int intFi = 3;
        private final long longFi = 4L;
        private String stringFi = "4hnbrqersefw";
        private final SampleEnum enumFi = SampleEnum.A;
    }

    static final class SampleObjectParamCtor {
        private final int intFi2;
        private final long longFi2;
        private final String stringFi2;
        private final SampleEnum enumFi2;

        SampleObjectParamCtor(int intFi2, long longFi2, String stringFi2, SampleEnum enumFi2) {
            this.intFi2 = intFi2;
            this.longFi2 = longFi2;
            this.stringFi2 = stringFi2;
            this.enumFi2 = enumFi2;
        }

        public int intFi2() {
            return intFi2;
        }

        public long longFi2() {
            return longFi2;
        }

        public String stringFi2() {
            return stringFi2;
        }

        public SampleEnum enumFi2() {
            return enumFi2;
        }
    }

    record SampleRecord(int intFi3, long longFi3, String stringFi3, SampleEnum enumFi3) {}

    enum SampleEnum {
        A, C, B
    }

    static class Man {
        private String name;
        private int age;
        private List<String> favoriteBooks;

        public Man(String name, int age, List<String> favoriteBooks) {
            this.name = name;
            this.age = age;
            this.favoriteBooks = favoriteBooks;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public List<String> getFavoriteBooks() {
            return favoriteBooks;
        }

        public void setFavoriteBooks(List<String> favoriteBooks) {
            this.favoriteBooks = favoriteBooks;
        }
    }
}
