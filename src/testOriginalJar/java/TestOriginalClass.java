public class TestOriginalClass {
    private void privateVoid() {
        System.out.println("Original");
    }

    public int publicInt() {
        return 2;
    }

    protected static String protectedStatic() {
        return "original";
    }

    int packageArgs(int arg1, int arg2) {
        return arg1 + arg2;
    }

    public int notOverwritten() {
        return 123;
    }

}
