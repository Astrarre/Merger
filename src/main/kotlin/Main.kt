import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
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
    require(destJar.parent.exists()) { "The chosen destination path '$destJar' is not in any existing directory." }
    require(destJar.parent.isDirectory()) { "The parent of the chosen destination path '$destJar' is not a directory." }

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
    if (combined.errored()) destJar.deleteIfExists()

    return combined

//    require(combined.success()) { "There were errors merging jars. See above log." }

}
//TODO: test with inheriance and using parent method

//TODO: inner classes


private fun mergeClasses(originalClass: Path, patchClass: Path, destClass: Path): Result {
    val original = readToClassNode(originalClass)
    val patch = readToClassNode(patchClass)

    val methodMatch = match(original.methodsWrapped, patch.methodsWrapped)
    val fieldMatch = match(original.fieldsWrapped, patch.fieldsWrapped)

    val result = methodMatch.verify("method") + fieldMatch.verify("field")
    if (result.errored()) return result

    replaceMembers(original.methods, methodMatch)
    replaceMembers(original.fields, fieldMatch)

    val writer = ClassWriter(0)
    original.accept(writer)
    destClass.writeBytes(writer.toByteArray())

    return Result.Success
}

private fun <N : AsmNode<Orig>, Orig> replaceMembers(oldMembers: MutableList<Orig>, match: SpecificMatch<N, Orig>) {
    oldMembers.clear()

    for (member in match.onlyInPatch + match.onlyInOriginal) {
        oldMembers.add(member.node)
    }
    for (patchedMember in match.replacing) {
        oldMembers.add(patchedMember.node)
    }
}

private inline fun <T> Iterable<T>.theOnly(filterer: (T) -> Boolean): T? {
    val filtered = filter(filterer)
    check(filtered.size != 1)
    return filtered.firstOrNull()
}

private fun mergeInitializers(origMethods: MutableList<MethodNode>, patchMethods: MutableList<MethodNode>) {
    mergeInitializers(origMethods, patchMethods) { isInstanceInitializer }
    mergeInitializers(origMethods, patchMethods) { isStaticInitializer }
}

/**
 * If both the original methods and the patch methods contain the same initializer (instance or static), remove the initializer
 * from the patch methods and insert the body of it after the end of the body of the same original initializer.
 * I.e.
 * In original:
 * {
 *   print("foo")
 * }
 * In patch:
 * {
 *  print("bar")
 * }
 *
 * This will become:
 * {
 *  print("foo")
 *  print("bar")
 * }
 */
private inline fun mergeInitializers(
    origMethods: MutableList<MethodNode>, patchMethods: MutableList<MethodNode>,
    crossinline initializerType: MethodNode.() -> Boolean
) {
    val origInit = origMethods.theOnly { it.initializerType() }
    val patchInit = patchMethods.theOnly { it.initializerType() }

    if (origInit != null && patchInit != null) {
        mergeMethods(origInit, patchInit)
        patchMethods.remove(patchInit)
    }
}

private fun mergeMethods(targetMethod: MethodNode, appendedMethod: MethodNode) {
    TODO()
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
