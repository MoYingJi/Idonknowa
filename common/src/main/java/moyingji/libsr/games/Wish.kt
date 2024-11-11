package moyingji.libsr.games

import moyingji.lib.api.*
import moyingji.lib.collections.TwoList
import moyingji.libsr.games.Wish.WishItem
import org.jetbrains.annotations.Contract
import kotlin.random.*

class Wish<T>(
    var gacha: Gacha<T>,
    val data: WishData = WishData()
) : Iterator<WishItem<T>> {
    override fun hasNext(): Boolean = true
    override fun next(): WishItem<T> = gacha.next(data)
    fun getAdditions(item: WishItem<T>): List<T> = gacha.getAdditions(item)
    fun nextList(): WishItem<List<T>> = next().let { WishItem(mutableListOf(it.item).apply { addAll(getAdditions(it)) }, it.star, it.isUp) }

    data class WishItem<T>(
        val item: T,
        val star: UByte,
        val isUp: Boolean = false
    )

    data class WishData(
        var last5: UShort = 0u,
        var last4: UShort = 0u,
        var lastUp5: UByte = 0u,
        var lastUp4: UByte = 0u
    )

    open class Gacha<T>(val parent: Gacha<out T>? = null) {
        open val pool3: List<T> = listOf(); get() = if (parent == null) field else TwoList(parent.pool3, field)
        open val pool4: List<T> = listOf(); get() = if (parent == null) field else TwoList(parent.pool4, field)
        open val pool5: List<T> = listOf(); get() = if (parent == null) field else TwoList(parent.pool5, field)

        open fun next(@Mutable data: WishData): WishItem<T> {
            val star = nextStar(data)
            val item = getPool(star).random()
            if (star != 5u.toUByte()) data.last5 ++ else data.last5 = 0u
            if (star != 4u.toUByte()) data.last4 ++ else data.last4 = 0u
            return WishItem(item, star)
        }
        open fun getAdditions(item: WishItem<T>): List<T> = listOf()

        @Contract(pure = false)
        fun nextStar(@Immutable data: WishData): UByte {
            val s5 = next5(data.last5)
            val s4 = next4(data.last4)
            val r = Random.nextUInt(1000u)
            return when (r) {
                in 0u until s5 -> 5u
                in s5 until s5+s4 -> 4u
                else -> 3u
            }
        }
        @Contract(pure = true)
        fun getPool(star: UByte): List<T> = when (star.toUInt()) {
            3u -> pool3
            4u -> pool4
            5u -> pool5
            else -> throw IllegalArgumentException("Invalid star: $star")
        }

        // region 概率表
        @Contract(pure = true)
        fun next5(last5: UShort): UInt = when (last5) {
            in 0u..72u -> 6u
            in 73u..88u -> (last5-72u)*60u+6u
            in 89u..90u -> 1000u
            else -> 0u }
        @Contract(pure = true)
        fun next4(last4: UShort): UInt = when (last4) {
            in 0u..6u -> 60u
            // 四星概率表很难查 这是猜的概率
            in 7u..8u -> (last4-6u)*400u+60u
            in 9u..10u -> 1000u
            else -> 0u }
        // endregion
    }

    open class UpGacha<T>(parent: Gacha<out T>? = null) : Gacha<T>(parent) {
        open val poolUp4: List<T> = listOf()
        open val poolUp5: List<T> = listOf()

        override fun next(@Mutable data: WishData): WishItem<T> {
            val star = nextStar(data)
            var isUp = false
            val item = when (star.toUInt()) {
                4u -> if (poolUp4.isNotEmpty() && (data.lastUp4 > 0u || Random.nextBoolean()))
                    poolUp4.also { isUp = true } else
                        pool4.also { if (poolUp4.isNotEmpty()) data.lastUp4 ++ }
                5u -> if (poolUp5.isNotEmpty() && (data.lastUp5 > 0u || Random.nextBoolean()))
                    poolUp5.also { isUp = true } else
                        pool5.also { if (poolUp5.isNotEmpty()) data.lastUp5 ++ }
                else -> getPool(star)
            }.random()
            if (star != 5u.toUByte()) data.last5 ++ else data.last5 = 0u
            if (star != 4u.toUByte()) data.last4 ++ else data.last4 = 0u
            return WishItem(item, star, isUp)
        }
    }
}