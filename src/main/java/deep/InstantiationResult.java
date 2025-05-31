package deep;

sealed interface InstantiationResult {
    record Success(Object instance) implements InstantiationResult {}

    record NullPointer(NullPointerException exception) implements InstantiationResult {}

    record GeneralFailure(Exception exception) implements InstantiationResult {}
}
