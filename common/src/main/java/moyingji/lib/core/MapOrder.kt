package moyingji.lib.core

import moyingji.lib.core.MapOrder.ElementOrder.*
import moyingji.lib.core.MapOrder.IndexOrder.*
import moyingji.lib.math.Vec2i
import moyingji.lib.util.swap

enum class MapOrder(
    val indexOrder: IndexOrder,
    val elementOrder: ElementOrder
) {
    XA(XY, AZ),
    YA(YX, AZ),
    XZ(XY, ZA),
    YZ(YX, ZA);

    fun <T> access(list: List<List<T>>, pair: Vec2i): T
    = indexOrder.access(list, pair, elementOrder)

    fun <T> accessNullable(list: List<List<T>>, pair: Vec2i): T?
    = indexOrder.accessNullable(list, pair, elementOrder)

    fun <T> mutate(list: MutableList<MutableList<T>>, pair: Vec2i, value: T): T
    = indexOrder.mutate(list, pair, value, elementOrder)

    enum class ElementOrder {
        AZ, ZA;

        fun apply(index: Int = 0, diff: Int = 1): Int
        = when (this) { AZ -> index + diff; ZA -> index - diff }

        fun <T> access(list: List<T>, index: Int)
        : T = when (this) {
            AZ -> list[index]
            ZA -> list[list.size-1-index]
        }
        fun <T> accessNullable(list: List<T>, index: Int)
        : T? = when (this) {
            AZ -> list.getOrNull(index)
            ZA -> list.getOrNull(list.size-1-index)
        }
        fun <T> mutate(list: MutableList<T>, index: Int, value: T)
        : T = when (this) {
            AZ -> list.set(index, value)
            ZA -> list.set(list.size-1-index, value)
        }
    }

    enum class IndexOrder {
        XY, YX;

        fun apply(pair: Pair<Int, Int>): Pair<Int, Int>
        = when (this) { XY -> pair; YX -> pair.swap() }

        fun <T> access(
            list: List<List<T>>,
            pair: Vec2i,
            order: ElementOrder = AZ
        ): T = if (this == YX) XY.access(list, pair.swap(), order)
        else order.access(order.access(list, pair.first), pair.second)

        fun <T> accessNullable(
            list: List<List<T>>,
            pair: Vec2i,
            order: ElementOrder = AZ
        ): T? = if (this == YX) XY.accessNullable(list, pair.swap(), order)
        else order.accessNullable(list, pair.first)?.let { order.accessNullable(it, pair.second) }

        fun <T> mutate(
            list: List<MutableList<T>>,
            pair: Vec2i, value: T,
            order: ElementOrder = AZ
        ): T = if (this == YX) XY.mutate(list, pair.swap(), value, order)
        else order.mutate(order.access(list, pair.first), pair.second, value)
    }
}