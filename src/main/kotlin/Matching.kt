data class Match<N : AsmNode<*>>(
    val common: Map<N, N>,
    val onlyInOriginal: Collection<N>,
    val onlyInPatch: Collection<N>
) {
    val replacing = common.values
}

val AsmNode<*>.isConstructor
    get() = when (this) {
        is ClassAsmNode -> false
        is FieldAsmNode -> false
        is MethodAsmNode -> node.name == "<init>"
    }

fun AsmNode<*>.hasAnnotation(annotation : String) = annotations.any { it.desc == annotation }

fun <N : AsmNode<*>> match(originals: List<N>, patches: List<N>): Match<N> {
    val origMap = originals.map { it.uniqueIdentifier to it }.toMap()
    val patchMap = patches.map { it.uniqueIdentifier to it }.toMap()

    val common = origMap
        .filter { (id, node) -> if (node.isConstructor) node.hasAnnotation(ReplaceAnnotationDesc) else patchMap.containsKey(id) }
        .map { it.value to patchMap.getValue(it.key) }
        .toMap()

    // Keys == originals, values == patch
    val onlyInOrig = origMap.filter { !common.containsKey(it.value) }
    val onlyInPatch = patchMap.filter { !common.containsValue(it.value) }

    return Match(common = common, onlyInOriginal = onlyInOrig.values, onlyInPatch = onlyInPatch.values)
}

