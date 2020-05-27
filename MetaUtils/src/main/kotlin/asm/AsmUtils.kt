package asm

import org.objectweb.asm.tree.MethodNode

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