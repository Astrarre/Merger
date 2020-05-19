
public class TestAccess {
    private int privatePublicField;
    public int publicPrivateField;

    private int privatePublicMethod() {
        return 1;
    }

    public static int publicPrivateMethod() {
        return 2;
    }
}
