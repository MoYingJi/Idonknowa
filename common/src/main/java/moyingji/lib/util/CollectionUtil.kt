package moyingji.lib.util

inline fun <reified T> Iterable<*>.findType(): T? = find { it is T }.typed()
