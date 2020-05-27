import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.InputStream
import java.nio.file.*
import java.util.jar.JarOutputStream

fun Path.exists() = Files.exists(this)
fun Path.deleteIfExists() = Files.deleteIfExists(this)
inline fun Path.openJar(usage: (FileSystem) -> Unit) = FileSystems.newFileSystem(this, null).use(usage)
fun Path.walk(usage: (Path) -> Unit) = Files.walk(this).forEach(usage)
fun Path.createJar() = JarOutputStream(Files.newOutputStream(this)).close()
fun Path.isDirectory() = Files.isDirectory(this)
fun Path.createDirectory(): Path = Files.createDirectory(this)
fun Path.inputStream(): InputStream = Files.newInputStream(this)
fun Path.writeBytes(bytes: ByteArray): Path = Files.write(this, bytes)
inline fun openJars(jar1: Path, jar2: Path, jar3: Path, usage: (FileSystem, FileSystem, FileSystem) -> Unit) =
    jar1.openJar { j1 -> jar2.openJar { j2 -> jar3.openJar { j3 -> usage(j1, j2, j3) } } }

fun Path.copyTo(target: Path, overwrite: Boolean = true): Path? {
    var args = arrayOf<CopyOption>()
    if (overwrite) args += StandardCopyOption.REPLACE_EXISTING
    return Files.copy(this, target, *args)
}

fun readToClassNode(classFile: Path): ClassNode = classFile.inputStream().use { stream ->
    ClassNode().also { ClassReader(stream).accept(it, 0) }
}