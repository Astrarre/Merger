import ladder.merger.annotations.Replace;

public class TestInitializers {
    @Replace
    private int instanceFieldNoInitializerInitializer = 1;
    @Replace
    public int instanceFieldInitializerInitializer = 2;
    @Replace
    public int instanceFieldInitializerNoInitializer;
    @Replace
    public int instanceFieldNoInitializerNoInitializer;
    @Replace
    public static String staticFieldNoInitializerInitializer = "ReplacedNoInit";
    @Replace
    public static String staticFieldInitializerInitializer = "ReplaceInit";
    @Replace
    public static String staticFieldInitializerNoInitializer;
    @Replace
    public static String staticFieldNoInitializerNoInitializer;
}
