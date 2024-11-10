package moyingji.idonknowa.core

import moyingji.idonknowa.Formatting
import moyingji.idonknowa.core.Refinable.DataBuilder.*
import moyingji.idonknowa.datagen.withFlatModel
import moyingji.idonknowa.items.*
import moyingji.idonknowa.lang.*
import moyingji.idonknowa.serialization.*
import moyingji.idonknowa.util.*
import moyingji.lib.api.Final
import moyingji.lib.inspiration.MutableMapsMap
import moyingji.lib.util.*
import net.minecraft.ChatFormatting.*
import net.minecraft.world.item.*
import net.minecraft.world.level.ItemLike

interface Refinable : ItemLike, TooltipUtil, StackInitListener, StackCustomRarity {
    val translationKey: TranslationKey get() = this.asItem().descriptionId
        .tranKey().suffix("refine")
    val refineName: TranslationKey get() = translationKey.suffix("name")
    val refineDesc: TranslationKey get() = translationKey.suffix("description")
    val refineMaxLevel: UByte get() = 5u

    val refineData: MutableMapsMap<String, UByte, String>

    val ItemStack.refinable: Refinable get() = item as? Refinable ?: this@Refinable

    var ItemStack.refineLevel: UByte
        get() = this.getOrDefault(REFINE_LEVEL.value(), 1u)
        set(value) { this.set(REFINE_LEVEL.value(),
            if (value in 1u..refineMaxLevel.toUInt()) value else 1u) }

    fun ItemStack.checkRefineLevel() {
        if (refineLevel > refineMaxLevel) refineLevel = refineMaxLevel
        if (refineLevel <= 0u) refineLevel = 1u
    }

    fun ItemStack.refineData(key: String): String?
    = with(refinable) { refineData(key, refineLevel) }

    fun refineData(key: String, level: UByte): String?
    = if (!refineData.containsKey(key)) null else refineData(refineData[key], level)
    fun refineData(keyData: MutableMap<UByte, String>, level: UByte)
    : String? = keyData[level] ?: if (level <= 1u)
        null else refineData(keyData, level.dec())

    fun addToRefinableSet() { refinables += this }
    override fun initedItemStack(stack: ItemStack)
    { with(stack.refinable) { reloadRefineLevel(stack) } }

    override fun getRarity(stack: ItemStack): Rarity
    = stack.rarityComponent // 不被附魔影响稀有度


    // region Refine Tooltip
    val autoAppendRefineTooltip: Boolean get() = true
    override fun appendTooltip(tooltip: TooltipArgs) {
        super.appendTooltip(tooltip)
        if (autoAppendRefineTooltip)
            tooltip.stack.refinable.appendRefineTooltip(tooltip)
    }

    fun appendRefineTooltip(tooltip: TooltipArgs) {
        appendRefineTitle(tooltip)
        appendRefineLevel(tooltip)
        pressShiftToDisplayDetails(tooltip) {
            appendRefineDescription(tooltip) }
    }
    fun appendRefineTitle(tooltip: TooltipArgs) {
        " 「${refineName.value}」".text()
            .withStyle(Formatting.AQUA)
            .also { tooltip += it } }
    fun appendRefineLevel(tooltip: TooltipArgs) {
        val stack = tooltip.stack
        val color = Formatting.LIGHT_PURPLE
        Translations.REFINE_LEVEL.templated.replace(
            "level", stack.refineLevel.str
                .prefix(YELLOW.toString())
                .suffix(color.toString())
        ).value.text().withStyle(color)
            .also {
                stack.checkRefineLevel()
                if (stack.refineLevel == refineMaxLevel)
                    it.append(" [MAX]".textStyle(GOLD))
            }.also { tooltip += it }
    }
    fun appendRefineDescription(tooltip: TooltipArgs)
    { with(tooltip.stack.refinable) {
        val desc = refineDesc.templatedLines
        val data = refineData
        for ((k, keyData) in data)
            refineData(keyData, tooltip.stack.refineLevel)
                ?.let { desc.replace(k, it.let(::fixRefineDescriptionArgument)) }
        desc.value.lines().forEach { tooltip += "$refineDescBackground$it".text() }
    } }
    val refineDescArgColor: String get() = YELLOW.str
    val refineDescBackground: String get() = GRAY.str
    fun fixRefineDescriptionArgument(string: String): String {
        val translated = language.getOrDefault(string)
        val colored = "$refineDescArgColor$translated$refineDescBackground"
        return colored
    }
    // endregion


    // region Upgrade

    fun getUpgradeLevel(addition: ItemStack)
    : UByte = if (addition isOf this) addition.refineLevel else 0u
    fun ItemStack.upgradeRefine(level: UByte): ItemStack
    = this.also { with(refinable) { refineLevel = (refineLevel+level).toUByte()
        .also { reloadRefineLevel(this@upgradeRefine) } } }
    /** 非必要不重写 */ fun canBeRefineBase(base: ItemStack): Boolean
    = with(base.refinable) { base.refineLevel in 1u until refineMaxLevel.toUInt() }

    @Final fun canBeRefineAddition(addition: ItemStack)
    : Boolean = getUpgradeLevel(addition) > 0u
    /** 非必要不重写 */ fun upgradeRefine(
        stack: ItemStack, addition: ItemStack,
        needsCopy: Boolean = false
    ): ItemStack {
        val s = if (needsCopy) stack.copy() else stack
        return s.upgradeRefine(stack.refinable.getUpgradeLevel(addition))
    }
    fun getDefaultInstance(level: UByte): ItemStack
    = this.default.let { if (level > 1u) it.upgradeRefine(level.dec()) else it }

    fun reloadRefineLevel(stack: ItemStack) {}
    // endregion


    companion object {
        val REFINE_LEVEL: NbtTypeRegS<UByte> by nbtType(ModCodecs.UBYTE_P)

        val refineTemplate: RegHelper<Item> = RegHelper
            .item { ModSmithingTemplate {
                key = "item.idonknowa.refine_template".tranKey()
                baseIcon += ModSmithingTemplate.EMPTY_SLOT_ALL
                additionIcon += ModSmithingTemplate.EMPTY_SLOT_ALL
                defaultFormatting()
            } }
            .withFlatModel()

        val refinables: MutableSet<Refinable> = mutableSetOf()

        fun <M: MutableMapsMap<String, UByte, String>> M.level(level: Number, f: (Pairs) -> Unit)
        : M = this.also { DataBuilder(this).level(level.toByte().toUByte(), f) }
        fun <M: MutableMapsMap<String, UByte, String>> M.key(key: Any?, f: (Key) -> Unit)
        : M = this.also { DataBuilder(this).key(key.str, f) }
    }

    class DataBuilder(val map: MutableMapsMap<String, UByte, String>) {
        fun level(level: UByte, f: (Pairs) -> Unit) { f(Pairs(map, level)) }
        fun key(key: String, f: (Key) -> Unit) { f(Key(map, key)) }

        class Pairs(val map: MutableMapsMap<String, UByte, String>, val level: UByte) {
            operator fun plusAssign(pair: Pair<String, Any?>)
            { map[pair.first][level] += pair.second.str }
        }
        class Key(val map: MutableMapsMap<String, UByte, String>, val key: String) {
            operator fun plusAssign(pair: Pair<Number, Any?>)
            { map[key][pair.first.toByte().toUByte()] = pair.second.str }
        }
    }
}