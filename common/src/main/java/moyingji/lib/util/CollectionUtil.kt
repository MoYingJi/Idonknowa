package moyingji.lib.util

import moyingji.lib.api.Mutable
import moyingji.lib.collections.*

fun <T> Iterator<T>.iterable(): Iterable<T> = Iterable { this }

fun <T> Iterator<T>.take(count: Int): Iterator<T> = CounteredIterator(this, count)

@Mutable fun <T, R> @receiver:Mutable MutableCollection<T>.map(
    from: (T) -> R, to: (R) -> T, cache: Boolean = false, toCache: Boolean = false
): MutableCollection<R> { require(cache || !toCache)
    return if (!cache) MapdCollection(this, from, to)
    else MapdCachedCollection(this, from, to, toCached = toCache)
}