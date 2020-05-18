import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarOutputStream

private fun Path.exists() = Files.exists(this)

fun merge(originalJar: Path, patchJar: Path, destJar: Path) {
    JarOutputStream(Files.newOutputStream(destJar)).close()

    FileSystems.newFileSystem(destJar, null).use {
        FileSystems.newFileSystem(originalJar, null).use { originalFs ->
            FileSystems.newFileSystem(patchJar, null).use { patchFs ->
                Files.walk(patchFs.getPath("/")).forEach {
                    println("Found file in jar: $it")
                    if (!it.endsWith(".class")) return@forEach

                    val originalClass = originalFs.getPath(it.toString())
                    val patchClass = originalFs.getPath(it.toString())

                    assert(patchClass.exists())
                    require(originalClass.exists()) {
                        throw IllegalArgumentException("Found class in patch jar which does not exist in original jar: $patchClass")
                    }
                }
            }
        }
    }

}

private fun mergeClasses(originalClass: Path, patchClass: Path) {
    val original = ClassReader(Files.newInputStream(originalClass))
    val patch = ClassReader(Files.newInputStream(patchClass))

}

object PatchVisitor : ClassVisitor(Opcodes.ASM8) {
    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String,
        exceptions: Array<String>
    ): MethodVisitor {
        println("Visiting method: $name")
        return super.visitMethod(access, name, descriptor, signature, exceptions)
    }
}