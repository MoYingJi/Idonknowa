package moyingji.lib.game.wish

import moyingji.lib.inspiration.WeightedRandom

open class WishGacha<T>(
    open val item3: List<T> = listOf(),
    open val item4: List<T> = listOf(),
    open val item5: List<T> = listOf(),
    open val up4: List<T> = listOf(),
    open val up5: List<T> = listOf(),
) {
    open fun next(args: WishArgs): WishResult<T> {
        require (args.data.hasNext())
        var data = args.data
        val rarity = generateRarity(args)

        val ngu = when (rarity) {
            WishRarity.R3 -> false
            WishRarity.R4 -> up4.isNotEmpty()
            WishRarity.R5 -> up5.isNotEmpty()
            else -> throw IllegalStateException()
        }
        val isUp = ngu && generateIsUp(args, rarity)

        if (rarity == WishRarity.R5) {
            if (isUp) data = data.clearUp5()
            else if (ngu) data = data.noUp5()
            data = data.clear5()
        } else data = data.no5()

        if (rarity == WishRarity.R4) {
            if (isUp) data = data.clearUp4()
            else if (ngu) data = data.noUp4()
            data = data.clear4()
        } else data = data.no4()

        val item = generateItem(rarity, isUp, args.random)
        data = data.next()
        return WishResult(data, item, isUp)
    }


    open fun weight4(last: Int): Int = when (last) {
        in 0..4 -> 700
        in 5..8 -> (last - 4) * 2000
        else -> throw IllegalArgumentException()
    }
    open fun weight5(last: Int): Int = when (last) {
        in 0..53 -> 100
        in 54..64 -> 100 + (last - 53) * 900
        else -> throw IllegalArgumentException()
    }
    open fun generateRarity(args: WishArgs): UByte {
        val data = args.data
        if (data.last5 >= 64u) return 5u
        if (data.last4 >=  8u) return 4u
        // random
        val rw = WeightedRandom<UByte>(args.random)
        rw.add(5u, weight5(data.last5.toInt()), 10000)
        rw.add(4u, weight4(data.last4.toInt()), 10000)
        rw.completeTo(10000, 3u)
        return rw.generate(10000)
    }

    open fun generateIsUp(args: WishArgs, rarity: UByte): Boolean
    = when (rarity) {
        WishRarity.R3 -> false
        WishRarity.R4 -> args.data.lastUp4 >= 2u || args.random(2) == 0
        WishRarity.R5 -> args.data.lastUp5 >= 2u || args.random(2) == 0
        else -> throw IllegalArgumentException()
    }

    open fun generateItem(
        rarity: UByte, isUp: Boolean,
        random: (until: Int) -> Int
    ): T {
        val items = when (rarity) {
            WishRarity.R3 -> item3
            WishRarity.R4 -> if (isUp) up4 else item4
            WishRarity.R5 -> if (isUp) up5 else item5
            else -> throw IllegalArgumentException()
        }
        return items[random.invoke(items.size)]
    }


    open class Mutable<T> (
        override val item3: MutableList<T> = mutableListOf(),
        override val item4: MutableList<T> = mutableListOf(),
        override val item5: MutableList<T> = mutableListOf(),
        override val up4: MutableList<T> = mutableListOf(),
        override val up5: MutableList<T> = mutableListOf(),
    ) : WishGacha<T> (item3, item4, item5, up4, up5)
}
