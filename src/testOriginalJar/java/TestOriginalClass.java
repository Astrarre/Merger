public class TestOriginalClass {

    private void privateVoid() {
        System.out.println("Original");
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

    public int notOverwritten() {
        return 123;
    }

    public static void main(String[] args) {
        TestOriginalClass test = new TestOriginalClass();
        test.privateVoid();
        System.out.println(test.publicInt());
        System.out.println(protectedStatic());
        System.out.println(test.packageArgs(5, 10));
        System.out.println(test.notOverwritten());
    }
}
