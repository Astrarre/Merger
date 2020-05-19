import org.objectweb.asm.tree.MethodNode

data class SpecificMatch<N : AsmNode<Orig>, Orig>(
    val common: Map<N, N>,
    val onlyInOriginal: Collection<N>,
    val onlyInPatch: Collection<N>
) {
    val replacing = common.values
}

typealias Match<N> = SpecificMatch<N, *>

val AsmNode<*>.isInitializer
    get() = when (this) {
        is ClassAsmNode -> false
        is FieldAsmNode -> false
        is MethodAsmNode -> node.desc == "()V" && (node.name == "<init>" || node.name == "<clinit>")
    }

val MethodNode.isConstructor get() = name == "<init>"
val MethodNode.isVoid get() = desc == "()V"
val MethodNode.isInstanceInitializer get() = isVoid && isConstructor
val MethodNode.isStaticInitializer get() = name == "<clinit>"

fun AsmNode<*>.hasAnnotation(annotation: String) = annotations.any { it.desc == annotation }

fun <N : AsmNode<Orig>, Orig> match(originals: List<N>, patches: List<N>): SpecificMatch<N, Orig> {
    val origMap = originals.map { it.uniqueIdentifier to it }.toMap()
    val patchMap = patches.map { it.uniqueIdentifier to it }.toMap()

    val common = origMap
        .filter { (id, node) ->
            if (node.isInitializer) node.hasAnnotation(ReplaceAnnotationDesc) else patchMap.containsKey(
                id
            )
        }
        .map { it.value to patchMap.getValue(it.key) }
        .toMap()

    // Keys == originals, values == patch
    val onlyInOrig = origMap.filter { !common.containsKey(it.value) }
    val onlyInPatch = patchMap.filter { !common.containsValue(it.value) }

    return SpecificMatch(common = common, onlyInOriginal = onlyInOrig.values, onlyInPatch = onlyInPatch.values)
}

