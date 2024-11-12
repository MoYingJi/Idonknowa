package moyingji.lib.util

import moyingji.lib.api.Mutable
import moyingji.lib.collections.*

fun <T> Iterator<T>.iterable(): Iterable<T> = Iterable { this }

fun <T> Iterator<T>.take(count: Int): Iterator<T> = CounteredIterator(this, count)


// region xmap (Mutable)
@Mutable fun <T, R> MutableCollection<T>.map(
    from: (T) -> R, to: (R) -> T, cache: Boolean = false, toCache: Boolean = false
): MutableCollection<R> { require(cache || !toCache)
    return if (!cache) MapdCollection(this, from, to)
    else MapdCachedCollection(this, from, to, toCached = toCache)
}
@Mutable fun <T, R> MutableList<T>.map(
    from: (T) -> R, to: (R) -> T, cache: Boolean = false, toCache: Boolean = false
): MutableList<R> { require(cache || !toCache)
    if (!cache) return MapdMutableList(this, from, to)
    return MapdCachedMutableList(this, from, to, toCached = toCache)
}
// 与普通的单向 kt std map 函数区分
@Mutable fun <T, R> MutableCollection<T>.xmap(
    cache: Boolean = false, toCache: Boolean = false, from: (T) -> R
): ExpectTo<(R) -> T, MutableCollection<R>> {
    require(cache || !toCache)
    return ExpectTo { to -> map(from, to, cache, toCache) }
}
@Mutable fun <T, R> MutableList<T>.xmap(
    cache: Boolean = false, toCache: Boolean = false, from: (T) -> R
): ExpectTo<(R) -> T, MutableList<R>> {
    require(cache || !toCache)
    return ExpectTo { to -> map(from, to, cache, toCache) }
}
// endregion