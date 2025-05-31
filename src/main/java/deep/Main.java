package deep;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        Man original = new Man("Sid", 91, List.of("dark-blue", "the second"));
        Man twin = DeepCopyMaker.deepCopy(original);

        System.out.println("Original %s: (%s, %s, %s)".formatted(original, original.getAge(), original.getName(), original.getFavoriteBooks()));
        System.out.println("Twin %s: (%s, %s, %s)".formatted(twin, twin.getAge(), twin.getName(), twin.getFavoriteBooks()));
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

        public int getAge() {
            return age;
        }

        public List<String> getFavoriteBooks() {
            return favoriteBooks;
        }
    }
}
