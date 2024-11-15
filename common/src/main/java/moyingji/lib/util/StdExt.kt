package moyingji.lib.util

import moyingji.lib.core.MapOrder

internal typealias APair<T> = Pair<T, T>

fun <K, V> Map<K, V>.firstKeyOfOrNull(value: V): K?
= this.entries.firstOrNull { it.value == value }?.key
fun <K, V> Map<K, V>.firstKeyOf(value: V): K
= this.entries.first { it.value == value }.key

fun <T> MutableIterable<T>.forEachRemove(f: (T) -> Unit)
{ removeAll { f(it); true } }

inline fun <T> T.alsoIf(c: Boolean, f: (T) -> Unit): T = also { if (c) f(it) }

inline fun <T, reified R> Array<T>.mapArray(f: (T) -> R): Array<R> = Array(size) { f(this[it]) }
inline fun <T, reified R> List<T>.mapArray(f: (T) -> R): Array<R> = Array(size) { f(this[it]) }

operator fun String.times(i: Int) = this.repeat(i)

fun <T> APair<T>.swap(order: MapOrder.IndexOrder = MapOrder.IndexOrder.YX)
: APair<T> = when (order) {
    MapOrder.IndexOrder.XY -> second to first
    MapOrder.IndexOrder.YX -> first to second
}
fun <A, B> Pair<A, B>.swap(): Pair<B, A> = second to first

fun <T, R> APair<T>.map(f: (T) -> R): APair<R> = f(first) to f(second)

fun Result<Boolean?>.isTrue(): Boolean = getOrNull() == true
fun Result<Boolean?>.isFalse(): Boolean = getOrNull() == false

fun <T> ifOrNull(c: Boolean, f: () -> T?): T? = if (c) f() else null