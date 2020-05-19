import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.reflect.Member
import java.lang.reflect.Modifier
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile

fun getResource(path: String) = Paths.get(
    TestJars::class.java
        .classLoader.getResource("dummyResource")!!.toURI()
).parent.resolve(path)

fun debugResultJar(jar: Path) {
    val targetDir = jar.parent.resolve(jar.toFile().nameWithoutExtension)
    targetDir.toFile().deleteRecursively()
    unzipJar(targetDir.toString(), jar.toString())
}

fun unzipJar(destinationDir: String, jarPath: String) {
    val file = File(jarPath)
    val jar = JarFile(file)

    // fist get all directories,
    // then make those directory on the destination Path
    run {
        val enums: Enumeration<JarEntry> = jar.entries()
        while (enums.hasMoreElements()) {
            val entry = enums.nextElement() as JarEntry
            val fileName = destinationDir + File.separator.toString() + entry.name
            val f = File(fileName)
            if (fileName.endsWith("/")) {
                f.mkdirs()
            }
        }
    }

    //now create all files
    val enums: Enumeration<JarEntry> = jar.entries()
    while (enums.hasMoreElements()) {
        val entry = enums.nextElement() as JarEntry
        val fileName = destinationDir + File.separator.toString() + entry.name
        val f = File(fileName)
        if (!fileName.endsWith("/")) {
            val `is`: InputStream = jar.getInputStream(entry)
            val fos = FileOutputStream(f)

            // write contents of 'is' to 'fos'
            while (`is`.available() > 0) {
                fos.write(`is`.read())
            }
            fos.close()
            `is`.close()
        }
    }
}

//        val child = URLClassLoader(
//            arrayOf<URL>(dest.toUri().toURL()),
//            this.javaClass.classLoader
//        )
//        val classToLoad = Class.forName("TestOriginalClass", true, child)
//        val method: Method = classToLoad.getDeclaredMethod("publicInt")
//        val instance = classToLoad.newInstance()
//        val value: Any = method.invoke(instance)
//        println(value)


@DslMarker
annotation class JarDsl

@JarDsl
fun testJar(jar: Path, init: TestJar.() -> Unit): TestJar {
    return TestJar(
        URLClassLoader(
            arrayOf<URL>(jar.toUri().toURL()),
            TestJar::class.java.classLoader
        )
    ).apply(init)
}

@JarDsl
class TestJar(private val classLoader: ClassLoader) {
    fun inClass(className: String, init: TestClass.() -> Unit = {}) =
        TestClass(Class.forName(className, true, classLoader), classLoader).apply(init)

//    fun instance()
}

val Member.isPublic get() = Modifier.isPublic(this.modifiers)
val Member.isPrivate get() = Modifier.isPrivate(this.modifiers)

interface ClassContext {
    val clazz: Class<*>
    val instance: Any?

    fun field(name: String) = clazz.getDeclaredField(name)
    fun method(name: String, argTypes: List<Class<*>> = listOf()) = try {
        clazz.getDeclaredMethod(name, *argTypes.toTypedArray())
    } catch (e: NoSuchMethodException) {
        clazz.getMethod(name, *argTypes.toTypedArray())
    }

    operator fun <T> String.invoke(argTypes: List<Class<*>> = listOf(), vararg args: Any?) =
        method(this, argTypes).let {
            it.isAccessible = true
            it.invoke(instance, *args)
        } as T

    operator fun <T> get(name: String) = field(name).let {
        it.isAccessible = true
        it.get(instance)
    } as T

    operator fun set(name: String, value: Any) = field(name).let {
        it.isAccessible = true
        it.set(instance, value)
    }
}

@JarDsl
class TestClass(override val clazz: Class<*>, val classLoader: ClassLoader) : ClassContext {
    override val instance: Any? = null
    inline fun withInstance(className: String? = null, init: TestInstance.() -> Unit = {}) = TestInstance(clazz,
        className?.let { Class.forName(className, true, classLoader) } ?: clazz.newInstance()
    ).apply(init)
}

@JarDsl
class TestInstance(override val clazz: Class<*>, override val instance: Any) : ClassContext