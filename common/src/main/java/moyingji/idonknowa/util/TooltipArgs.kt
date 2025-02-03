package moyingji.idonknowa.util

import moyingji.idonknowa.mia.CIR
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.*
import net.minecraft.item.Item.TooltipContext
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text

open class TooltipArgs(
    val stack: ItemStack,
    val context: TooltipContext,
    val tooltip: MutableList<Text>,
    val type: TooltipType
) : MutableList<Text> by tooltip {
    val item: Item = stack.item
    open val holder: Entity? get() = stack.holder
    open val player: PlayerEntity? get() = holder as? PlayerEntity

    class Mixin(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType,
        val cir: CIR<MutableList<Text>>,
        override val player: PlayerEntity?
    ) : TooltipArgs(
        stack, context, tooltip, type
    )
}
