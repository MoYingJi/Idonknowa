package moyingji.idonknowa.mia

import moyingji.idonknowa.items.RestoreAfterDied
import moyingji.idonknowa.items.RestoreAfterDied.Companion.restoreAfterDied
import moyingji.lib.math.Vec2i
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack

class PlayerInvMixinImpl(val self: Inventory) {
    constructor(self: Any) : this(self as Inventory)
    private val restore: MutableMap<Vec2i, ItemStack> = mutableMapOf()

    fun dropAllHead(l: MutableList<out MutableList<ItemStack>>) {
        for ((i1, cl) in l.withIndex())
            for ((i2, s) in cl.withIndex())
                if (shouldRestore(s)) {
                    l[i1][i2] = ItemStack.EMPTY
                    restore += i1 to i2 to s }
    }
    fun dropAllReturn(l: MutableList<out MutableList<ItemStack>>) {
        restore.forEach {
            val (v, s) = it
            val (i1, i2) = v
            l[i1][i2] = s
        }
    }

    fun shouldRestore(stack: ItemStack): Boolean {
        val item = stack.item
        if (item is RestoreAfterDied && item
            .shouldRestoreAfterDied(stack, self))
            return true
        if (stack.restoreAfterDied)
            return true
        return false
    }
}