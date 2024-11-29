@file:Suppress("NOTHING_TO_INLINE")
package moyingji.lib.math

import moyingji.lib.api.Immutable
import moyingji.lib.core.*
import moyingji.lib.util.*

// region Vec2 = Pair Number
typealias Vec2i = APair<Int>
typealias Vec2ui = APair<UInt>
typealias Vec2s = APair<Short>
typealias Vec2us = APair<UShort>

/** 对 [Pair]<[Comparable]> 取最大值 相等时取 [Pair.first] */
fun <T: Comparable<T>> APair<T>.max(): T = max(first, second)
/** 对 [Pair]<[Comparable]> 取最小值 相等时取 [Pair.first] */
fun <T: Comparable<T>> APair<T>.min(): T = min(first, second)

operator fun Vec2i.plus(other: Vec2i): Vec2i =
    first + other.first to second + other.second
operator fun Vec2i.times(scalar: Int): Vec2i =
    first * scalar to second * scalar
infix fun Vec2i.transform(scalar: Vec2i): Vec2i = Vec2i(
    scalar.first * second - scalar.second * first,
    scalar.second * first + scalar.first * second)
// endregion

/** 对 [Comparable] 取最大值 相等时取 [a] */
fun <T: Comparable<T>> max(a: T, b: T): T = if (a >= b) a else b
/** 对 [Comparable] 取最小值 相等时取 [a] */
fun <T: Comparable<T>> min(a: T, b: T): T = if (a <= b) a else b

operator fun Int.plus(other: UShort): Int = this + other.toInt()


// region Range
val <T: Comparable<T>> ClosedRange<T>.s: T inline get() = start
val <T: Comparable<T>> ClosedRange<T>.e: T inline get() = endInclusive

val ClosedRange<Int>.size: Int inline get() = e - s

inline fun <T: Comparable<T>> ClosedRange<T>.asClosedRange(): ClosedRange<T> = typed()
inline fun ClosedRange<Int>.toIntRange(): IntRange = this as? IntRange ?: s..e
infix fun <T: Comparable<T>> @Immutable ClosedRange<T>.extendTo(value: T)
: ClosedRange<T> = when {
    this.isEmpty() -> value..value
    value < s -> value..e
    value > e -> s..value
    else -> this
}
infix fun @Immutable IntRange.extendTo(value: Int): IntRange
= this.asClosedRange().extendTo(value).toIntRange()
// endregion

// range Clamp
fun <T: Comparable<T>> T.clamp(min: T?, max: T?): T {
    var value = this
    if (min != null) value = max(value, min)
    if (max != null) value = min(value, max)
    return value
}
fun <T: Comparable<T>> T.clamp(range: ClosedRange<T>): T = when {
    this < range.s -> range.s
    this > range.e -> range.e
    else -> this
}
// endregion


/**
 * 一个螺旋搜索迭代器 坐标可正可负 不限制搜索范围
 * @param first 首次移动方向
 * @param order 坐标系顺序
 * @param next 下一个方向 (默认为顺时针旋转)
 */
fun spiralSearch(
    first: Direction2D = Direction2D.W,
    order: MapOrder = MapOrder.XZ, // 按坐标系顺序
    next: (Direction2D) -> Direction2D
        = { it.last }, // 默认顺时针 (it.next 为逆时针)
): Sequence<Vec2i> {
    var pos = 0 to 0
    var direction: Direction2D = first
    var steps = 1; var stepCount = 0; var turnCount = 0
    return sequence { while (true) {
        yield(pos)
        pos = direction.next(pos, order)
        // 判断方向更换
        stepCount ++
        if (stepCount == steps) {
            direction = next(direction)
            turnCount ++; stepCount = 0
            if (turnCount % 2 == 0) steps ++
        }
    } }
}


// region randomWithWeight
fun <T> randomWithWeight(
    collection: Collection<T>,
    random: (until: Int) -> Int,
    weight: (T) -> Int,
): T {
    collection.isNotEmpty() || throw NoSuchElementException()
    val l: MutableList<T> = mutableListOf()
    for (i in collection) repeat(weight(i)) { l.add(i) }
    return l.random(random)
}
fun <T> randomWithWeight(
    array: Array<T>,
    random: (until: Int) -> Int,
    weight: (T) -> Int,
): T {
    array.isNotEmpty() || throw NoSuchElementException()
    val l: MutableList<T> = mutableListOf()
    for (i in array) repeat(weight(i)) { l.add(i) }
    return l.random(random)
}
// endregion



// region 压缩 pair first second
// 以下为 AI 生成
// region Vec2i <-> Long
fun Vec2i.toLong(): Long = (first.toLong() shl 32) or (second.toLong())
fun Long.toVec2i(): Vec2i = pairFirst() to pairSecond()
fun Long.pairFirst(): Int = (this shr 32).toInt() // 取高 32 位
fun Long.pairSecond(): Int = this.toInt() // 取低 32 位
// endregion
// region Vec2ui <-> ULong
fun Vec2ui.toULong(): ULong = (first.toULong() shl 32) or (second.toULong())
fun ULong.toVec2ui(): Vec2ui = pairFirst() to pairSecond()
fun ULong.pairFirst(): UInt = (this shr 32).toUInt()
fun ULong.pairSecond(): UInt = this.toInt().toUInt()
// endregion
// region Vec2s <-> Int
fun Vec2s.toInt(): Int = (first.toInt() shl 16) or (second.toInt())
fun Int.toVec2s(): Vec2s = pairFirst() to pairSecond()
fun Int.pairFirst(): Short = (this shr 16).toShort()
fun Int.pairSecond(): Short = this.toShort()
// endregion
// region Vec2us <-> UInt
fun Vec2us.toUInt(): UInt = (first.toUInt() shl 16) or (second.toUInt())
fun UInt.toVec2us(): Vec2us = pairFirst() to pairSecond()
fun UInt.pairFirst(): UShort = (this shr 16).toUShort()
fun UInt.pairSecond(): UShort = this.toUShort()
// endregion
// endregion
