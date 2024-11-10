@file:Suppress("NOTHING_TO_INLINE")
package moyingji.lib.math

import moyingji.lib.api.Mutable
import moyingji.lib.core.Direction2D
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

inline fun <T: Comparable<T>> ClosedRange<T>.asClosedRange(): ClosedRange<T> = typed()
inline fun ClosedRange<Int>.toIntRange(): IntRange =
    if (this is IntRange) this else s..e
infix fun <T: Comparable<T>> ClosedRange<T>.extendTo(value: T)
: ClosedRange<T> = when {
    this.isEmpty() -> value..value
    value < s -> value..e
    value > e -> s..value
    else -> this
}
infix fun IntRange.extendTo(value: Int): IntRange
= this.asClosedRange().extendTo(value).toIntRange()
// endregion


/**
 * 一个螺旋搜索迭代器 坐标可正可负 不限制搜索范围
 * @param visited 已访问过的点
 * @param predicate 搜索条件
 */
fun spiralSearch(
    @Mutable visited: MutableCollection<Vec2i> = mutableSetOf(),
    visitUntested: Boolean = false,
    predicate: (Vec2i) -> Boolean = { it !in visited }
): Iterator<Vec2i> {
    var x = 0; var y = 0
    var direction: Direction2D = Direction2D.W
    var steps = 1; var stepCount = 0; var turnCount = 0
    return iterator { while (true) {
        val v = x to y
        if (visitUntested) visited += v
        if (predicate(v)) {
            if (!visitUntested) visited += v
            yield(v) }
        val (mx, my) = direction.next()
        x += mx; y += my
        // 判断方向更换
        stepCount ++
        if (stepCount == steps) {
            turnCount ++; stepCount = 0
            direction = direction.last
            if (turnCount % 2 == 0) steps ++
        }
    } }
}



// region 压缩 pair first second
// 以下为 AI 生成
// region Vec2i <-> Long
fun Vec2i.toLong(): Long = (first.toLong() shl 32) or (second.toLong())
fun Long.toVec2i(): Vec2i = pairFirst() to pairSecond()
fun Long.pairFirst(): Int = this.toInt() // 取高 32 位
fun Long.pairSecond(): Int = (this shr 32).toInt() // 取低 32 位
// endregion
// region Vec2ui <-> ULong
fun Vec2ui.toULong(): ULong = (first.toULong() shl 32) or (second.toULong())
fun ULong.toVec2ui(): Vec2ui = pairFirst() to pairSecond()
fun ULong.pairFirst(): UInt = this.toInt().toUInt()
fun ULong.pairSecond(): UInt = (this shr 32).toUInt()
// endregion
// region Vec2s <-> Int
fun Vec2s.toInt(): Int = (first.toInt() shl 16) or (second.toInt())
fun Int.toVec2s(): Vec2s = pairFirst() to pairSecond()
fun Int.pairFirst(): Short = this.toShort()
fun Int.pairSecond(): Short = (this shr 16).toShort()
// endregion
// region Vec2us <-> UInt
fun Vec2us.toUInt(): UInt = (first.toUInt() shl 16) or (second.toUInt())
fun UInt.toVec2us(): Vec2us = pairFirst() to pairSecond()
fun UInt.pairFirst(): UShort = this.toUShort()
fun UInt.pairSecond(): UShort = (this shr 16).toUShort()
// endregion
// endregion