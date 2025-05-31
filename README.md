## Deep Copy Utility

This project offers a `deepCopy()` method to create deep copies of Java objects with any structure. It works reliably for a wide range of cases, including:

* Primitive fields and their wrappers
* Arrays of primitives and objects
* Records and plain Java objects (POJOs)
* Nested objects and cyclic object graphs

It uses reflection and object graph traversal without any external libraries or serialization tricks.

### Key Features

* ✅ Uses only **J2SE standard library**
* ✅ Fully compatible with **Java 21**
* ✅ Avoids `Serializable` and `Cloneable` hacks
* ✅ Includes a working `main()` method demonstrating usage
* ✅ Correctly handles cycles and preserves shared references
* ⚠️ Known limitation: May produce inconsistent results for objects whose constructors perform non-trivial logic, 
  such as injecting special marker objects that should not be copied or performing parameter-based calculations.
  This limitation affects certain internal JDK classes, 
  leading to inconsistent copying (e.g. `ImmutableCollections.List12` with 1 parameter) or failure (e.g. `Locale`).
  These scenarios are not addressed in the current implementation to prioritize simplicity and maintainability.

### Example

```java
public class Main {
    public static void main(String[] args) {
        ComplexObject original = new ComplexObject();
        ComplexObject copy = DeepCopyMaker.deepCopy(original);

        System.out.println("Original: " + original);
        System.out.println("Copy:     " + copy);
    }
}
```

### How to Test

1. Run tests with Maven:

   ```bash
   mvn test
   ```
2. Run the `Main` class:

   ```bash
   mvn clean compile
   java --add-opens java.base/java.util=ALL-UNNAMED -cp target/classes deep.Main
   ```
