import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Paths
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile


class SomeTest {

    private fun getResource(path: String) = Paths.get(
        SomeTest::class.java
            .classLoader.getResource(path)!!.toURI()
    )

    @Test
    fun test() {
        val original = getResource("original-jar.jar")
        val patch = getResource("patch-jar.jar")
        val dest = patch.parent.resolve("mergedJar.jar")
        val result = merge(original, patch, dest)
        val mergedExtracted = patch.parent.resolve("mergedJar")
        mergedExtracted.toFile().deleteRecursively()
        unzipJar(mergedExtracted.toString(), dest.toString())
    }

    private fun unzipJar(destinationDir: String, jarPath: String) {
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
}