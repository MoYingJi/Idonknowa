package moyingji.idonknowa.all

import moyingji.idonknowa.autoreg.*
import moyingji.idonknowa.datagen.LangProvider.C.en
import moyingji.idonknowa.datagen.LangProvider.C.zh
import moyingji.idonknowa.datagen.model.*
import net.minecraft.item.Item
import net.minecraft.util.Rarity

object ModItems {
    val PRIMOGEM: RegS<Item> by item {
        fireproof()
        rarity(Rarity.RARE)
    } tran {
        zh to { "原石" }
        en to { "Primogem" }
    } model generated
}
