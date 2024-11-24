package moyingji.idonknowa.util

import net.minecraft.world.Container
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

operator fun Container.get(index: Int): ItemStack = this.getItem(index)
operator fun Container.set(index: Int, item: ItemStack) { this.setItem(index, item) }
operator fun Container.iterator(): Iterator<ItemStack> = object : Iterator<ItemStack> {
    var index = 0
    override fun hasNext(): Boolean = index < containerSize
    override fun next(): ItemStack = getItem(index++)
}
fun Container.firstOrNull(f: (ItemStack) -> Boolean): ItemStack?
{ for (s in this) if (f(s)) return s; return null }



interface ModContainer : Container {
    /** 调用于 [moyingji.idonknowa.mia.mixin.SlotMixin.mayPlace] */
    fun mayPlace(slot: Slot, stack: ItemStack): Boolean? = null

    /** 调用于 [moyingji.idonknowa.mia.mixin.SlotMixin.isActive] */
    fun isActive(slot: Slot): Boolean? = null
}