package moyingji.lib.game.wish

import kotlin.random.Random

data class WishData (
    val leftTotal: UInt = 0u,
    val left: UInt = 0u,
    val last4: UShort = 0u,
    val last5: UShort = 0u,
    val lastUp4: UByte = 0u,
    val lastUp5: UByte = 0u,
) {
    fun hasNext(): Boolean = left > 0u && leftTotal > 0u

    fun next(): WishData = if (hasNext()) this.copy(
        left = left - 1u, leftTotal = leftTotal - 1u
    ) else throw IllegalStateException()

    fun no4(): WishData = this.copy(last4 = (last4 + 1u).toUShort())
    fun no5(): WishData = this.copy(last5 = (last5 + 1u).toUShort())
    fun noUp4(): WishData = this.copy(lastUp4 = (lastUp4 + 1u).toUByte())
    fun noUp5(): WishData = this.copy(lastUp5 = (lastUp5 + 1u).toUByte())
    fun clear4(): WishData = this.copy(last4 = 0u)
    fun clear5(): WishData = this.copy(last5 = 0u)
    fun clearUp4(): WishData = this.copy(lastUp4 = 0u)
    fun clearUp5(): WishData = this.copy(lastUp5 = 0u)
}

data class WishResult<T> (
    val data: WishData,
    val result: T,
    val isUp: Boolean,
)

data class WishArgs (
    var data: WishData,
    val random: (until: Int) -> Int = Random::nextInt
)

object WishRarity {
    const val R3: UByte = 3u
    const val R4: UByte = 4u
    const val R5: UByte = 5u
}
