package moyingji.idonknowa.core

import moyingji.idonknowa.core.refine.Refine
import moyingji.lib.game.wish.WishGacha
import net.minecraft.loot.LootTable
import net.minecraft.util.Identifier

object Regs {
    val REFINE: MutableMap<Identifier, Refine> = mutableMapOf()
    val GACHA: MutableMap<Identifier, WishGacha<LootTable>> = mutableMapOf()
}