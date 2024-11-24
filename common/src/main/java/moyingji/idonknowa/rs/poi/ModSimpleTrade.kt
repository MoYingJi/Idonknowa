package moyingji.idonknowa.rs.poi

import dev.architectury.registry.level.entity.trade.TradeRegistry
import moyingji.idonknowa.core.RegS
import moyingji.idonknowa.util.*
import moyingji.lib.util.toOptional
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.npc.*
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.trading.*
import net.minecraft.world.level.ItemLike

interface IModTrade : VillagerTrades.ItemListing {
    fun getPriceA(trader: Entity, random: RandomSource): ItemCost?
    fun getPriceB(trader: Entity, random: RandomSource): ItemCost?
    fun getSale(trader: Entity, random: RandomSource): ItemStack?
    fun getMaxTrade(trader: Entity, random: RandomSource): Int
    fun getExperiencePoints(trader: Entity, random: RandomSource): Int
    fun getPriceMultiplier(trader: Entity, random: RandomSource): Number
    fun getLevel(profession: VillagerProfession): Int
    override fun getOffer(trader: Entity, random: RandomSource): MerchantOffer?
    = MerchantOffer(
        getPriceA(trader, random).let { it ?: return null },
        getPriceB(trader, random).toOptional(),
        getSale(trader, random).let { if (it == null || it.isEmpty) return null else it },
        getMaxTrade(trader, random),
        getExperiencePoints(trader, random),
        getPriceMultiplier(trader, random).toFloat())
    infix fun reg(v: VillagerProfession) { TradeRegistry
        .registerVillagerTrade(v, getLevel(v), this) }
}


class ModSimpleTrade : IModTrade {
    var price: ItemCost? get() = priceA; set(value) { priceA = value }
    var priceA: ItemCost? = null
    var priceB: ItemCost? = null
    var sale: ItemStack? = null
    /** 最大交易次数 */    var maxTrade: Int = 0
    /** 交易后获得经验 */  var experiencePoints: Int = 0 // in 2..30
    /** 交易后涨价 */     var priceMultiplier: Number = 0 // in 0.05 .. 0.2
    /** 交易等级 */      var level: Int = 1 // must in 1..5 (流浪商人不填)
    override fun getPriceA(trader: Entity, random: RandomSource): ItemCost? = priceA
    override fun getPriceB(trader: Entity, random: RandomSource): ItemCost? = priceB
    override fun getSale(trader: Entity, random: RandomSource): ItemStack? = sale
    override fun getMaxTrade(trader: Entity, random: RandomSource): Int = maxTrade
    override fun getExperiencePoints(trader: Entity, random: RandomSource): Int = experiencePoints
    override fun getPriceMultiplier(trader: Entity, random: RandomSource): Number = priceMultiplier
    override fun getLevel(profession: VillagerProfession): Int = level
}
fun trade(f: ModSimpleTrade.() -> Unit = {}): ModSimpleTrade = ModSimpleTrade().apply(f)
fun VillagerProfession.trade(f: ModSimpleTrade.() -> Unit) { trade().apply(f) reg this }


class PriceRandomTrade(val parent: IModTrade? = null) : IModTrade {
    var rangeA: ClosedRange<Int>? = null
    var rangeB: ClosedRange<Int>? = null
    var rangeS: ClosedRange<Int>? = null
    var itemA: List<ItemLike> = listOf()
    var itemB: List<ItemLike> = listOf()
    var itemSale: List<ItemLike> = listOf()

    var range: ClosedRange<Int>? get() = rangeA; set(value) { rangeA = value }
    var item: List<ItemLike> get() = itemA; set(value) { itemA = value }
    var sale: Pair<List<ItemLike>, ClosedRange<Int>?> get() = itemSale to rangeS
        set(value) { itemSale = value.first; rangeS = value.second }
    var price: Pair<List<ItemLike>, ClosedRange<Int>?> get() = itemA to rangeA
        set(value) { itemA = value.first; rangeA = value.second }
    var priceA: Pair<List<ItemLike>, ClosedRange<Int>?> get() = itemA to rangeA
        set(value) { itemA = value.first; rangeA = value.second }
    var priceB: Pair<List<ItemLike>, ClosedRange<Int>?> get() = itemB to rangeB
        set(value) { itemB = value.first; rangeB = value.second }
    override fun getPriceA(trader: Entity, random: RandomSource): ItemCost?
    = random(parent?.getPriceA(trader, random), itemA, rangeA, random)
    override fun getPriceB(trader: Entity, random: RandomSource): ItemCost?
    = random(parent?.getPriceB(trader, random), itemB, rangeB, random)
    @Suppress("DEPRECATION")
    private fun random(parent: ItemCost?, item: List<ItemLike>, range: ClosedRange<Int>?, random: RandomSource): ItemCost? {
        @Suppress("NAME_SHADOWING")
        val item = if (item.isNotEmpty())
             random.nextInt(item.size).let { item[it] } else null
        if (range == null) {
            if (parent == null) return null
            if (item == null) return parent
            return ItemCost(item.asItem().builtInRegistryHolder(),
                parent.count, parent.components, parent.itemStack.transmuteCopy(item))
        }
        val r = random.nextIntBetweenInclusive(range.start, range.endInclusive)
        return if (item != null)
            ItemCost(item, r)
        else if (parent != null)
            ItemCost(parent.item, r, parent.components, parent.itemStack.count(r))
        else null
    }
    override fun getSale(trader: Entity, random: RandomSource): ItemStack? {
        val stack = parent?.getSale(trader, random)
        val item = if (itemSale.isNotEmpty())
            random.nextInt(itemSale.size).let { itemSale[it] } else null
        return if (stack != null) {
            if (item != null) stack.transmuteCopy(item) else stack
        } else {
            item?.asItem()?.default ?: return null
        }.let { s ->
            rangeS?.let { s.count(random.nextIntBetweenInclusive(it.start, it.endInclusive)) } ?: s
        }
    }

    var maxTrade: Int? = null
    var experiencePoints: Int? = null
    var priceMultiplier: Number? = null
    var level: Int? = null

    override fun getMaxTrade(trader: Entity, random: RandomSource): Int = maxTrade ?: parent?.getMaxTrade(trader, random) ?: 0
    override fun getExperiencePoints(trader: Entity, random: RandomSource): Int = experiencePoints ?: parent?.getExperiencePoints(trader, random) ?: 0
    override fun getPriceMultiplier(trader: Entity, random: RandomSource): Number = priceMultiplier ?: parent?.getPriceMultiplier(trader, random) ?: 0
    override fun getLevel(profession: VillagerProfession): Int = level ?: parent?.getLevel(profession) ?: 1

    fun arg(level: Int, maxTrade: Int, experiencePoints: Int, priceMultiplier: Number) {
        this.level = level
        this.maxTrade = maxTrade
        this.experiencePoints = experiencePoints
        this.priceMultiplier = priceMultiplier.toFloat().div(100F)
    }

    // region DSL Functions
    fun Int.exactlyRange(): IntRange = this..this
    fun RegS<out ItemLike>.listOfValue(): List<ItemLike> = object : AbstractList<ItemLike>() {
        override val size: Int = 1
        override fun get(index: Int): ItemLike = if (index == 0)
            value() else throw IndexOutOfBoundsException()
    }
    infix fun ItemLike.to(range: ClosedRange<Int>)
    : Pair<List<ItemLike>, ClosedRange<Int>> = listOf(this) to range
    infix fun RegS<out ItemLike>.to(range: ClosedRange<Int>)
    : Pair<List<ItemLike>, ClosedRange<Int>> = listOfValue() to range
    infix fun ItemLike.to(count: Int)
    : Pair<List<ItemLike>, ClosedRange<Int>> = listOf(this) to count.exactlyRange()
    infix fun RegS<out ItemLike>.to(count: Int)
    : Pair<List<ItemLike>, ClosedRange<Int>> = listOfValue() to count.exactlyRange()
    // endregion
}
infix fun IModTrade?.priceMod(f: PriceRandomTrade.() -> Unit)
: PriceRandomTrade = PriceRandomTrade(this).apply(f)
fun IModTrade?.priceMod(): PriceRandomTrade = priceMod {}
fun priceMod(f: PriceRandomTrade.() -> Unit) = null priceMod f
fun VillagerProfession.priceMod(f: PriceRandomTrade.() -> Unit) { null priceMod f reg this }

