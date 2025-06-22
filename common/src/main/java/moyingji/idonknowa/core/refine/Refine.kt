package moyingji.idonknowa.core.refine

import moyingji.idonknowa.core.Regs
import moyingji.idonknowa.lang.*
import moyingji.idonknowa.util.*
import moyingji.lib.util.firstKeyOf
import net.minecraft.item.ItemStack
import net.minecraft.util.*
import org.jetbrains.annotations.Contract

open class Refine(
    val maxRefineLevel: UByte = 5u,
    val doDurabilityMergeUpgrade: Boolean = true
) {
    open val id: Identifier by lazy { Regs.REFINE.firstKeyOf(this) }
    open val name: ITransKey = TransLazyKey { id.toTranslationKey("refine") }
    open val desc: ITransKey = TransLazyKey { name.key + ".desc" }

    val data: DataValues = DataValuesImpl(maxRefineLevel)

    open val tooltipBackground: Formatting = Formatting.GRAY
    open val tooltipDataColor: Formatting = Formatting.YELLOW

    open fun getUpgradeValue(other: ItemStack): UByte {
        val d = other.refineData
        if (d == null) return 0u
        if (this != d.refine) return 0u
        return d.level
    }
    open fun getOtherUpgradeDurabilityMerge(other: ItemStack): Int {
        if (other.isDamageable) return other.durability
        return 0
    }

    /**
     * 新物品应直接继承原物品 即 [stack] 的数据
     * 仅修改精炼等级和耐久度
     * 但 用于精炼的物品 即 [other] 数据不应该被继承
     * 包括附魔等其他属性都不会被继承 精炼等级和耐久度除外
     */
    @Contract("refinable_matched, matched -> new else fail")
    open fun upgrade(
        stack: ItemStack, other: ItemStack
    ): ItemStack {
        val r = stack.copy()
        // 精炼等级叠加
        val rd = stack.refineData!!
        require(this == rd.refine)
        val ul = getUpgradeValue(other)
        require(ul > 0u)
        val level = rd.level + ul
        r.refineData = rd.copy(level = if (level <= maxRefineLevel)
            level.toUByte() else maxRefineLevel)
        // 耐久度叠加
        if (doDurabilityMergeUpgrade && stack.isDamageable)
            r.durability = stack.durability + getOtherUpgradeDurabilityMerge(other)
        // 返回
        return r
    }

    open fun isDescNeedShift(): Boolean = true
    fun appendTooltip(tooltip: TooltipArgs, level: UByte) {
        // Title Line
        val levelColor = if (level == maxRefineLevel)
            Formatting.AQUA else Formatting.YELLOW
        val textLevel = " ($level)".text(levelColor)
        tooltip += name.text()
            .append(textLevel)
            .apply { if (level == maxRefineLevel)
                append(" [MAX]".text(Formatting.GOLD)) }
            .formatted(Formatting.LIGHT_PURPLE)
        // Desc
        detailsShift(
            tooltip, condition = ::isDescNeedShift
        ) { desc.tempValue.apply {
            data.keys.forEach {
                val value = data.getValue(level, it)!!
                this += it to "$tooltipDataColor$value$tooltipBackground"
            }
            for (l in value.lines())
                tooltip += l.text(tooltipBackground)
        } }
    }

    // region Data Values 存储各精炼等级对应的数据
    interface DataValues {
        val maxRefineLevel: UByte

        // 此处的值会从上一级继承 递归调用 如果一直没有值 则返回 null
        fun getValue(level: UByte, key: String): String?
        // 所有键 此处必须保证每个等级都有 (所以只取一级就行)
        val keys: Set<String>
    }
    interface MutableDataValues : DataValues {
        fun setValue(level: UByte, key: String, value: String?)
    }
    class DataValuesImpl(
        override val maxRefineLevel: UByte = 5u
    ) : MutableDataValues {
        val ar: Array<MutableMap<String, String>>
        = Array(maxRefineLevel.toInt()) { mutableMapOf() }

        override fun getValue(level: UByte, key: String): String? {
            val lower = (level - 1u).toUByte()
            return if (level < 1u || level > maxRefineLevel) null
            else ar[lower.toInt()][key] ?: getValue(lower, key)
        }
        override fun setValue(level: UByte, key: String, value: String?) {
            if (level < 1u || level > maxRefineLevel) return
            val l = ar[level.toInt() - 1]
            if (value == null) l.remove(key)
            else l[key] = value
        }
        override val keys: Set<String> get() = ar[0].keys
            // get() = ar.flatMap { it.keys }.distinct()
    }
    // endregion
}
