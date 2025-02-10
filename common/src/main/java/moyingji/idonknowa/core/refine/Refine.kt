package moyingji.idonknowa.core.refine

import moyingji.idonknowa.core.Regs
import moyingji.idonknowa.lang.*
import moyingji.idonknowa.util.*
import moyingji.lib.util.firstKeyOf
import net.minecraft.util.*

open class Refine(
    val maxRefineLevel: Int = 5
) {
    open val id: Identifier by lazy { Regs.REFINE.firstKeyOf(this) }
    open val name: ITransKey = TransLazyKey { id.toTranslationKey("refine") }
    open val desc: ITransKey = TransLazyKey { name.key + ".desc" }

    val data: DataValues = DataValuesImpl(maxRefineLevel)

    open val tooltipBackground: Formatting = Formatting.GRAY
    open val tooltipDataColor: Formatting = Formatting.YELLOW

    fun appendTooltip(tooltip: TooltipArgs, level: Int) {
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
        desc.tempValue.apply {
            data.keys.forEach {
                val value = data.getValue(level, it)!!
                this += it to "$tooltipDataColor$value$tooltipBackground"
            }
            value.lines().forEach {
                tooltip += it.text(tooltipBackground)
            }
        }
    }

    // region Data Values 存储各精炼等级对应的数据
    interface DataValues {
        val maxRefineLevel: Int

        // 此处的值会从上一级继承 递归调用 如果一直没有值 则返回 null
        fun getValue(level: Int, key: String): String?
        // 所有键 此处必须保证每个等级都有 (所以只取一级就行)
        val keys: Set<String>
    }
    interface MutableDataValues : DataValues {
        fun setValue(level: Int, key: String, value: String?)
    }
    class DataValuesImpl(
        override val maxRefineLevel: Int = 5
    ) : MutableDataValues {
        val ar: Array<MutableMap<String, String>>
        = Array(maxRefineLevel) { mutableMapOf() }

        override fun getValue(level: Int, key: String): String? {
            return if (level < 0 || level >= maxRefineLevel) null
            else ar[level][key] ?: getValue(level - 1, key)
        }
        override fun setValue(level: Int, key: String, value: String?) {
            if (level < 0 || level >= maxRefineLevel) return
            if (value == null) ar[level].remove(key)
            else ar[level][key] = value
        }
        override val keys: Set<String> get() = ar[0].keys
            // get() = ar.flatMap { it.keys }.distinct()
    }
    // endregion
}
