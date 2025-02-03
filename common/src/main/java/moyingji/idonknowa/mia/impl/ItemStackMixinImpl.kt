package moyingji.idonknowa.mia.impl

import moyingji.idonknowa.core.Events
import moyingji.idonknowa.mia.CIR
import moyingji.idonknowa.util.TooltipArgs
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text

object ItemStackMixinImpl {
    fun getTooltip(
        stack: ItemStack,
        content: TooltipContext,
        player: PlayerEntity?,
        type: TooltipType,
        cir: CIR<MutableList<Text>>,
        tooltip: MutableList<Text>,
        after: Boolean
    ) {
        val ta = TooltipArgs.Mixin(
            stack, content, tooltip, type, cir, player)
        val e = if (!after)
            Events.ItemTooltip.BEFORE_ITEM
        else Events.ItemTooltip.AFTER_ITEM
        e.invoker().invoke(ta)
    }
}
