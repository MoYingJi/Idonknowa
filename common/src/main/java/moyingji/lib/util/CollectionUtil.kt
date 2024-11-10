package moyingji.lib.util

import moyingji.lib.inspiration.MapdCachedCollection

fun <T> Iterator<T>.iterable(): Iterable<T> = Iterable { this }

fun <T, R> MutableCollection<T>.map(
    from: (T) -> R, to: (R) -> T
): MapdCachedCollection<T, R>
= MapdCachedCollection(this, from, to)