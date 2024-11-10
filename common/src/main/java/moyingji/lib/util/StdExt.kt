package moyingji.lib.util

internal typealias APair<T> = Pair<T, T>

fun <K, V> Map<K, V>.firstKeyOrNull(value: V): K?
= this.entries.firstOrNull { it.value == value }?.key
fun <K, V> Map<K, V>.firstKey(value: V): K
= this.entries.first { it.value == value }.key

fun <T> MutableIterable<T>.forEachRemove(f: (T) -> Unit)
{ removeAll { f(it); true } }

inline fun <T> T.alsoIf(c: Boolean, f: (T) -> Unit): T = also { if (c) f(it) }

operator fun String.times(i: Int) = this.repeat(i)

fun <A, B> Pair<A, B>.swap(): Pair<B, A> = second to first

fun Result<Boolean?>.isTrue(): Boolean = getOrNull() == true
fun Result<Boolean?>.isFalse(): Boolean = getOrNull() == false