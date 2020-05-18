import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarOutputStream

private fun Path.exists() = Files.exists(this)
private fun Path.deleteIfExists() = Files.deleteIfExists(this)
private fun Path.openJar(usage: (FileSystem) -> Unit) = FileSystems.newFileSystem(this, null).use(usage)
private fun Path.walk(usage: (Path) -> Unit) = Files.walk(this).forEach(usage)
private fun Path.createJar() = JarOutputStream(Files.newOutputStream(this)).close()
private fun Path.isDirectory() = Files.isDirectory(this)
private fun Path.createDirectory() = Files.isDirectory(this)
private fun Path.inputStream() = Files.newInputStream(this)
private fun Path.writeBytes(bytes: ByteArray) = Files.write(this, bytes)

//TODO: test with an "error" patch jar
//TODO: actually test the working patch jar using classloaders, and remove the usage of prints
//TODO: test fields
fun merge(originalJar: Path, patchJar: Path, destJar: Path): Result {
    destJar.deleteIfExists()
    destJar.createJar()

    val results = mutableListOf<Result>()
    destJar.openJar { newFs ->
        originalJar.openJar { originalFs ->
            patchJar.openJar { patchFs ->
                originalFs.getPath("/").walk { currentFile ->
                    if (currentFile.toString() == "/") return@walk

                    val originalFile = originalFs.getPath(currentFile.toString())
                    val patchFile = patchFs.getPath(currentFile.toString())
                    val newFile = newFs.getPath(currentFile.toString())

                    if (currentFile.isDirectory()) {
                        newFile.createDirectory()
                    }

                    assert(originalFile.exists())
                    assert(!newFile.exists())

                    if (currentFile.toString().endsWith(".class") && patchFile.exists()) {
                        results.add(mergeClasses(originalFile, patchFile, newFile))
                    } else {
                        Files.copy(originalFile, newFile)
                    }
                }
            }
        }
    }
    val combined = Result(results.flatMap { it.errors })

    for (error in combined.errors) {
        println(error)
    }

    return combined

//    require(combined.success()) { "There were errors merging jars. See above log." }

}

//TODO: inner classes
private fun mergeClasses(originalClass: Path, patchClass: Path, destClass: Path): Result {
    val original = readToClassNode(originalClass)
    val patch = readToClassNode(patchClass)

    val methodMatch = match(original.methodsWrapped, patch.methodsWrapped)
    val fieldMatch = match(original.fieldsWrapped, original.fieldsWrapped)

    val result = methodMatch.verify("method") + fieldMatch.verify("field")
    if (result.errored()) return result

//    original.methods.clear()
//    for (method in methodMatch.onlyInPatch + methodMatch.onlyInOriginal) {
//        original.methods.add(method.node)
//    }
//    for (patchedNode in methodMatch.replacing) {
//        original.methods.add(patchedNode.node)
//    }

    val writer = ClassWriter(0)
    original.accept(writer)
    destClass.writeBytes(writer.toByteArray())

    return Result.Success
}

private fun readToClassNode(classFile: Path): ClassNode = classFile.inputStream().use { stream ->
    ClassNode().also { ClassReader(stream).accept(it, 0) }
}

data class Result(val errors: List<String>) {
    fun errored(): Boolean = errors.isNotEmpty()
    fun success(): Boolean = !errored()
    operator fun plus(other: Result) = Result(this.errors + other.errors)

    companion object {
        val Success = Result(listOf())
    }
}

//TODO: verification with matching for inner classes

const val ReplaceAnnotationDesc = "Lladder/merger/annotations/Replace;"

fun Match<*>.verify(nodeName: String): Result {
    log(nodeName)

    // Verify that methods that are replacing are annotated with @Replace,
    // and methods that are not replacing are not annotated with it.
    val replacingWithNoAnnotation = replacing.filter { patch ->
        patch.annotations.none { it.desc == ReplaceAnnotationDesc }
    }
    val notReplacingWithAnnotation = onlyInPatch.filter { patch ->
        patch.annotations.any { it.desc == ReplaceAnnotationDesc }
    }

    cleanAnnotations()

    val errors = replacingWithNoAnnotation.map { patch ->
        "The ${patch.nodeName} '${patch.fullyQualifiedName}' is replacing a ${patch.nodeName} of the same name," +
                " but is not annotated with @Replace."
    } + notReplacingWithAnnotation.map { patch ->
        "The ${patch.nodeName} '${patch.fullyQualifiedName}' is annotated with @Replace but there is no ${patch.nodeName} of the same name."
    }

    return Result(errors)
}

private fun Match<*>.cleanAnnotations() = replacing.forEach { patch ->
    // Remove @Replace annotation
    patch.annotations.removeIf { it.desc == ReplaceAnnotationDesc }
}





private fun Match<*>.log(name: String) = println(
    "Found ${common.size} common $name(s), ${onlyInOriginal.size} unique old $name(s), " +
            "and ${onlyInPatch.size} unique new $name(s)."
)
