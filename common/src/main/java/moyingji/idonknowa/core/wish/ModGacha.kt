package moyingji.idonknowa.core.wish

import moyingji.idonknowa.core.Regs
import moyingji.idonknowa.util.id
import moyingji.lib.game.wish.WishGacha
import net.minecraft.loot.LootTable

object ModGacha {
    val Base: WishGacha.Mutable<LootTable> = WishGacha.Mutable()
    init { Regs.GACHA += "base".id() to Base }
    fun base (
        up4: List<LootTable>, up5: List<LootTable>
    ): WishGacha<LootTable> = WishGacha(
        Base.item3, Base.item4, Base.item5, up4, up5
    )
}
