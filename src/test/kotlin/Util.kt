import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile

fun getResource(path: String) = Paths.get(
    TestJars::class.java
        .classLoader.getResource("dummyResource")!!.toURI()).parent.resolve(path)

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

//fun testJar(jar : Path) : TestJar{
//    return TestJar(
//        URLClassLoader(
//        arrayOf<URL>(jar.toUri().toURL()),
//        this.javaClass.classLoader
//    )
//    )
//}

class TestJar(private val classLoader : ClassLoader)

class TestClass