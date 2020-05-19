import ladder.merger.annotations.Replace;

public class TestOriginalClass {
    private int instanceField;
    public static String staticField = "OriginalInitializer";
    @Replace
    public int notReplacingField;

    private void privateVoid() {
        instanceField = 7;
        notReplacingMethod();
    }

    public int publicInt() {
        return 2;
    }
    protected static String protectedStatic() {
        return "Original";
    }
    int packageArgs(int arg1, int arg2) {
        return arg1 + arg2;
    }

    @Replace
    public void notReplacingMethod(){
        notReplacingField = 23;
    }

    @Replace
    public TestOriginalClass(int x1, int x2, String x3){

    }

}
