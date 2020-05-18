import ladder.merger.annotations.Replace;

//TODO: test fields
public class TestOriginalClass {
    @Replace
    public TestOriginalClass(int param) {
        System.out.println("Patched param = " + param);
    }

    @Replace
    private void privateVoid() {
        System.out.println("Patched");
    }

    public int publicInt() {
        return utility();
    }

    protected static String protectedStatic() {
        return "Patched";
    }

    int packageArgs(int arg1, int arg2) {
        return arg1 * arg2;
    }

    private int utility() {
        return -10;
    }
}
