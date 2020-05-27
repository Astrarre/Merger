import asm.AsmNode
import asm.ClassAsmNode
import asm.FieldAsmNode
import asm.MethodAsmNode
import org.objectweb.asm.tree.MethodNode

data class SpecificMatch<N : AsmNode<Orig>, Orig>(
    val common: Map<N, N>,
    val onlyInOriginal: Collection<N>,
    val onlyInPatch: Collection<N>
) {
    val replacing = common.values
}

typealias Match<N> = SpecificMatch<N, *>



fun <N : AsmNode<Orig>, Orig> match(originals: List<N>, patches: List<N>): SpecificMatch<N, Orig> {
    val origMap = originals.map { it.uniqueIdentifier to it }.toMap()
    val patchMap = patches.map { it.uniqueIdentifier to it }.toMap()

    val common = origMap
        .filter { (id, node) ->
            /*if (node.isInitializer) node.hasAnnotation(ReplaceAnnotationDesc) else*/ patchMap.containsKey(
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

