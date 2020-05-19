public interface TestInterface {
    default int foo() {
        return 2;
    }

    static String bar() {
        return "original";
    }
}
