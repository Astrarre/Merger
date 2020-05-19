import ladder.merger.annotations.Replace;

public interface TestInterface {
    @Replace
    default int foo() {
        return 10;
    }

    @Replace
    static String bar() {
        return "Replaced";
    }
}
