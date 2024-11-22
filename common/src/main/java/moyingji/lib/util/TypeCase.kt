package moyingji.lib.util

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <T> Any?.typed(): T = this as T

inline fun <reified T> Any?.typeNullable(): T? = this as? T