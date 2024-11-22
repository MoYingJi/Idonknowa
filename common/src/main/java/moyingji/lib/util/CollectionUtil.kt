package moyingji.lib.util

import moyingji.lib.api.ExpectFrom
import moyingji.lib.api.ExpectXmap
import moyingji.lib.api.Mutable
import moyingji.lib.collections.*

fun <T> Iterator<T>.iterable(): Iterable<T> = Iterable { this }

fun <T> Iterator<T>.take(count: Int): Iterator<T> = CounteredIterator(this, count)

// region MutableIterable.removeFirst[OrNull] (predicate?)
fun <T> MutableIterable<T>.removeFirstOrNull(predicate: (T) -> Boolean): T? {
    val i = this.iterator()
    for (t in this.iterator()) if (predicate(t)) {
        i.remove(); return t }
    return null
}
fun <T> MutableIterable<T>.removeFirst(predicate: (T) -> Boolean): T
= removeFirstOrNull(predicate) ?: throw NoSuchElementException()

fun <T> MutableIterable<T>.removeFirstOrNull(): T? {
    val i = this.iterator()
    if (!i.hasNext()) return null
    return i.next().also { _ -> i.remove() }
}
fun <T> MutableIterable<T>.removeFirst(): T
= removeFirstOrNull() ?: throw NoSuchElementException()
// endregion

// region xmap (Mutable)
@Mutable fun <T, R> MutableCollection<T>.map(
    to: (T) -> R, from: (R) -> T, cache: Boolean = false, toCache: Boolean = false
): MutableCollection<R> { require(cache || !toCache)
    return if (!cache) MapdCollection(this, to, from)
    else MapdCachedCollection(this, to, from, toCached = toCache)
}
@Mutable fun <T, R> MutableList<T>.map(
    to: (T) -> R, from: (R) -> T, cache: Boolean = false, toCache: Boolean = false
): MutableList<R> { require(cache || !toCache)
    if (!cache) return MapdMutableList(this, to, from)
    return MapdCachedMutableList(this, to, from, toCached = toCache)
}
@Mutable fun <T, R> MutableList<T>.map(
    cache: Boolean = false, toCache: Boolean = false
): ExpectXmap<(T) -> R, (R) -> T, MutableList<R>>
= ExpectXmap { to, from -> map(to, from, cache, toCache) }

@Mutable fun <T, R> MutableCollection<T>.mapTo(
    cache: Boolean = false, toCache: Boolean = false, to: (T) -> R
): ExpectFrom<(R) -> T, MutableCollection<R>> {
    require(cache || !toCache)
    return ExpectFrom { from -> map(to, from, cache, toCache) }
}
@Mutable fun <T, R> MutableList<T>.mapTo(
    cache: Boolean = false, toCache: Boolean = false, to: (T) -> R
): ExpectFrom<(R) -> T, MutableList<R>> {
    require(cache || !toCache)
    return ExpectFrom { from -> map(to, from, cache, toCache) }
}
// endregion