package moyingji.idonknowa.util

import net.minecraft.util.Tuple

// region Tuple (Pair in Minecraft)
typealias MPair<A, B> = Tuple<A, B>

operator fun <A> MPair<A, *>.component1(): A = this.a
operator fun <B> MPair<*, B>.component2(): B = this.b

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
infix fun <A, B> A.tuple(b: B): MPair<A, B> = MPair(this, b)

fun <A, B> MPair<A, B>.toPair(): Pair<A, B> = a to b
fun <A, B> Pair<A, B>.toTuple(): MPair<A, B> = first tuple second
fun <A, B> Pair<A, B>.toMPair(): MPair<A, B> = first tuple second
// endregion