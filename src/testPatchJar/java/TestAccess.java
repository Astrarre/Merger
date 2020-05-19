import ladder.merger.annotations.Replace;

public class TestAccess {
    @Replace
    public int privatePublicField;
    @Replace
    private int publicPrivateField;

    @Replace
    public int privatePublicMethod() {
        return 1;
    }
    @Replace
    private static int publicPrivateMethod() {
        return 2;
    }
}
