public class TestInitializers {
    private int instanceFieldNoInitializerInitializer;
    public int instanceFieldInitializerInitializer = 3;
    public int instanceFieldInitializerNoInitializer = 33;
    public int instanceFieldNoInitializerNoInitializer;
    public static String staticFieldNoInitializerInitializer;
    public static String staticFieldInitializerInitializer = "OrigInitializerInitializer";
    public static String staticFieldInitializerNoInitializer = "OrigInitializerNoInitializer";
    public static String staticFieldNoInitializerNoInitializer;

    public String chainedInit = Integer.toString(Integer.parseInt(new String("123")), 10);
}
