package moyingji.lib.core

import moyingji.lib.core.MapOrder.ElementOrder.*
import moyingji.lib.core.MapOrder.IndexOrder.*
import moyingji.lib.util.swap

enum class MapOrder(
    val indexOrder: IndexOrder,
    val elementOrder: ElementOrder
) {
    XA(XY, AZ),
    YA(YX, AZ),
    XZ(XY, ZA),
    YZ(YX, ZA);

    fun <T> access(list: List<List<T>>, x: Int, y: Int): T
    = indexOrder.access(list, x, y, elementOrder)

    fun <T> accessNullable(list: List<List<T>>, x: Int, y: Int): T?
    = indexOrder.accessNullable(list, x, y, elementOrder)

    fun <T> mutate(list: MutableList<MutableList<T>>, x: Int, y: Int, value: T)
    = indexOrder.mutate(list, x, y, value, elementOrder)

    enum class ElementOrder {
        AZ, ZA;

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
        fun applyDiff(index: Int = 0, diff: Int = 1)
        : Int = when (this) {
            AZ -> index + diff
            ZA -> index - diff
        }
    }

    enum class IndexOrder {
        XY, YX;

        fun <T> access(
            list: List<List<T>>,
            x: Int, y: Int,
            order: ElementOrder = AZ
        ): T = when (this) {
            XY -> order.access(order.access(list, x), y) // list[x][y]
            YX -> order.access(order.access(list, y), x) // list[y][x]
        }
        fun <T> accessNullable(
            list: List<List<T>>,
            x: Int, y: Int,
            order: ElementOrder = AZ
        ): T? = when (this) {
            XY -> order.accessNullable(list, x)?.let { order.accessNullable(it, y) }
            YX -> order.accessNullable(list, y)?.let { order.accessNullable(it, x) }
        }
        fun <T> mutate(
            list: List<MutableList<T>>,
            x: Int, y: Int, value: T,
            order: ElementOrder = AZ
        ): T = when (this) {
            XY -> order.mutate(order.access(list, y), x, value)
            YX -> order.mutate(order.access(list, x), y, value)
        }
        fun apply(pair: Pair<Int, Int>): Pair<Int, Int>
        = when (this) {
            XY -> pair
            YX -> pair.swap()
        }
    }
}