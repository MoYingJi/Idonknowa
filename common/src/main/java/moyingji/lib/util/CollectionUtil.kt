package moyingji.lib.util

import moyingji.lib.inspiration.*

fun <T> Iterator<T>.iterable(): Iterable<T> = Iterable { this }

fun <T> Iterator<T>.take(count: Int): Iterator<T> = CounteredIterator(this, count)

fun <T, R> MutableCollection<T>.map(
    from: (T) -> R, to: (R) -> T
): MapdCachedCollection<T, R>
= MapdCachedCollection(this, from, to)