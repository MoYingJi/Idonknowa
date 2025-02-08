package moyingji.lib.util

inline fun <reified T> Iterable<*>.findType(): T? = find { it is T }.typed()

fun <K, V> Map<K, V>.firstKeyOf(value: V): K {
    for ((k, v) in this) if (v == value) return k
    throw NoSuchElementException("No such key of value $value")
}
