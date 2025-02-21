package moyingji.idonknowa.core.wish

import moyingji.idonknowa.core.Regs
import moyingji.idonknowa.lang.TransKey
import moyingji.idonknowa.lang.tran
import moyingji.lib.game.wish.WishGacha
import moyingji.lib.util.firstKeyOf
import net.minecraft.loot.LootTable

fun WishGacha.Mutable<LootTable>.tran(): TransKey
= Regs.REFINE.firstKeyOf(this).toTranslationKey("gacha").tran()
