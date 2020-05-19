import ladder.merger.annotations.Replace;

public class TestOriginalClass {
    @Replace
    private int instanceField;
    @Replace
    public static String staticField;

    public int notReplacingField;

    @Replace
    private void privateVoid() {
        instanceField = 12;
        notReplacingMethod();
    }

    @Replace
    public int publicInt() {
        return 69;
    }
    @Replace
    protected static String protectedStatic() {
        return "Replaced";
    }
    @Replace
    int packageArgs(int arg1, int arg2) {
        return arg1 * arg2;
    }

    public void notReplacingMethod(){
        notReplacingField = 23;
    }

}
