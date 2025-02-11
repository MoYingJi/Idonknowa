package moyingji.lib.util

import java.util.Optional

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <T> Any?.typed(): T = this as T

fun <T: Any> T?.toOptional(): Optional<T> = Optional.ofNullable(this)

object FuckersUtil {
    inline fun <T> T.equalsOverrideHelper(
        other: Any?,
        condition: T.(T) -> Boolean
    ): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return condition(this, other.typed())
    }
}
