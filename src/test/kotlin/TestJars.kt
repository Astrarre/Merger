import org.junit.Test
import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader
import kotlin.test.assertEquals


class TestJars {
    @Test
    fun testMerge() {
        val original = getResource("testOriginalJar.jar")
        val patch = getResource("testPatchJar.jar")
        val dest = patch.parent.resolve("mergedJar.jar")
        val result = merge(original, patch, dest)
        assert(result.success())
        debugResultJar(dest)

        val child = URLClassLoader(
            arrayOf<URL>(dest.toUri().toURL()),
            this.javaClass.classLoader
        )
        val classToLoad = Class.forName("TestOriginalClass", true, child)
        val method: Method = classToLoad.getDeclaredMethod("publicInt")
        val instance = classToLoad.newInstance()
        val value: Any = method.invoke(instance)
        println(value)
    }

    @Test
    fun testErrorJar() {
        val original = getResource("testOriginalJar.jar")
        val patch = getResource("testErrorPatchJar.jar")
        val dest = patch.parent.resolve("should_not_exist.jar")
        val result = merge(original, patch, dest)
        assert(result.errored())
        assertEquals(9, result.errors.size)
    }
}

