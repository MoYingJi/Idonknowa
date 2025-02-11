package moyingji.idonknowa.all

import moyingji.idonknowa.all.item._TestItem
import moyingji.idonknowa.autoreg.*
import moyingji.idonknowa.core.refine.RefineTempItem
import moyingji.idonknowa.datagen.lang.*
import moyingji.idonknowa.datagen.model.*
import moyingji.idonknowa.util.*
import net.minecraft.item.Item
import net.minecraft.util.Rarity

object ModItems {
    val PRIMOGEM: RegS<Item> by item {
        fireproof().rarity(Rarity.RARE)
    } tran {
        zh to "原石"
        en to "Primogem"
    } model generated

    val TEST_ITEM: RegS<Item> by item(::_TestItem) model basic("item/stick".id(null))

    val REFINE_TEMP: RegS<Item> by item(::RefineTempItem) model generated
}
