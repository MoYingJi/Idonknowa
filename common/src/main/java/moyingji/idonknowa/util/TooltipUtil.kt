package moyingji.idonknowa.util

import net.minecraft.item.Item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text

class TooltipUtil(
    val stack: ItemStack,
    val context: TooltipContext,
    val tooltip: MutableList<Text>,
    val type: TooltipType
) : MutableList<Text> by tooltip {
}