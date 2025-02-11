package moyingji.idonknowa.util

import net.minecraft.item.ItemStack

var ItemStack.durability: Int
    get() = maxDamage - damage
    set(value) { damage = maxDamage - value }
