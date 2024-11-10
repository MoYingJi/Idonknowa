package moyingji.idonknowa.util

import moyingji.idonknowa.*
import moyingji.idonknowa.core.Events
import moyingji.idonknowa.core.Events.ItemTooltip
import moyingji.idonknowa.lang.*
import moyingji.idonknowa.mixink.CIR
import moyingji.lib.util.*
import net.minecraft.ChatFormatting.*
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.*
import net.minecraft.world.item.Item.TooltipContext
import net.minecraft.world.item.crafting.Ingredient

open class TooltipArgs(
    val stack: ItemStack,
    val tooltip: MutableList<Text>,
    val context: TooltipContext,
    val flag: TooltipFlag
) {
    val item: Item = stack.item
    open val holder: Entity? get() = stack.entityRepresentation
    open val player: Player? get() = holder as? Player

    /** 非必要不使用 / 通过 Mixin 实现 */
    open fun cancel() { throw UnsupportedOperationException() }
    open val cancelable: Boolean = false

    operator fun plusAssign(text: Text) { tooltip += text }

    fun appendDurability(
        background: Formatting,
        style: Formatting
    ) { if (flag.isAdvanced) return
        Text.empty()
            .append("item.durability".tranText(
                stack.durability.str.withStyle(style, background),
                stack.maxDamage).withStyle(background))
            .also { tooltip += it } }
}

// region MixinTooltipArgs
class MixinTooltipArgs(
    stack: Any,
    context: TooltipContext,
    player: Player?,
    flag: TooltipFlag,
    private val cir: CIR<MutableList<Text>>,
    tooltip: MutableList<Text>,
) : TooltipArgs(stack as ItemStack, tooltip, context, flag) {
    override val holder: Entity? = player ?: super.holder
    override val cancelable: Boolean get() = cir.isCancellable
    override fun cancel() { cir.cancel() }
}
// endregion

interface TooltipUtil {
    /** 调用于 [Events.ItemTooltip.BEFORE_ITEM] */
    fun appendTooltip(tooltip: TooltipArgs) {}

    /** 调用于 [Events.ItemTooltip.AFTER_ITEM] */
    fun appendTooltipAfterDefault(tooltip: TooltipArgs) {}

    /** 调用于 [moyingji.idonknowa.mixin.ItemStackMixin.appendAdvancedDurability] */
    fun appendAdvancedDurability(stack: ItemStack): Boolean? = null

    // region Static
    companion object {
        init {
            ItemTooltip.BEFORE_ITEM.register {
                appendIdonknowaDesc(it, "pre")
                if (it.item is TooltipUtil)
                    it.item.appendTooltip(it)
            }
            ItemTooltip.AFTER_ITEM.register {
                if (it.item is TooltipUtil)
                    it.item.appendTooltipAfterDefault(it)
                appendIdonknowaDesc(it, "suf")
            }
        }
        val excludeIdonknowaDesc: MutableCollection<Ingredient> = mutableSetOf()
        private fun appendIdonknowaDesc(tooltip: TooltipArgs, suffix: String) {
            if (excludeIdonknowaDesc.any { it.test(tooltip.stack) })
                return
            val key = tooltip.item.tranKey()
                .suffix("idonknowa.desc").suffix(suffix)
            if (key.hasLines) key.lines.lines().forEach {
                tooltip.tooltip += it.textStyle(GRAY) }
            else if (key.has) key.value.textStyle(GRAY)
                .also { tooltip.tooltip += it }
            else if (key.suffix("shift").hasLines)
                pressShiftToDisplayDetails(tooltip) {
                    key.suffix("shift").lines.lines().forEach {
                        tooltip.tooltip += it.textStyle(GRAY) } }
        }
    }
    // endregion
}

// region getIdonknowaDescKey
fun Item.idonknowaDescPre(): TranKey = tranKey()
    .suffix("idonknowa.desc.pre")
fun Item.idonknowaDescSuf(): TranKey = tranKey()
    .suffix("idonknowa.desc.suf")
fun Item.idonknowaDescPreShift(): TranKey = tranKey()
    .suffix("idonknowa.desc.pre.shift")
fun Item.idonknowaDescSufShift(): TranKey = tranKey()
    .suffix("idonknowa.desc.suf.shift")
// endregion

fun String.withStyle(style: Formatting, other: Formatting): String = "$style$this$other"
// = this.prefix(style.str).suffix(other.str)