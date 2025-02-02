package moyingji.idonknowa.all

import moyingji.idonknowa.autoreg.*
import moyingji.idonknowa.datagen.LangProvider.C.en
import moyingji.idonknowa.datagen.LangProvider.C.zh
import net.minecraft.item.Item

object ModItem {
    val PRIMOGEM: RegS<Item> by item() tran {
        zh to { "原石" }
        en to { "Primogem" }
    }
}