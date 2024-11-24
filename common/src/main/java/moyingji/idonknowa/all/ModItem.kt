package moyingji.idonknowa.all

import moyingji.idonknowa.Idonknowa
import moyingji.idonknowa.core.*
import moyingji.idonknowa.datagen.withFlatModel
import moyingji.idonknowa.items.*
import net.minecraft.world.item.*

typealias ItemSettings = Item.Properties

object ModItem {
    val PRIMOGEM: RegS<Item> by RegHelper
        .itemSettings { rarity(Rarity.UNCOMMON).fireResistant() }
        .withFlatModel()
        .listen { Idonknowa.info("Idonknowa listen Primogem!") }

    val MORA: RegS<Item> by RegHelper.itemSettings().withFlatModel()
    val INTERTWINED_FATE: RegS<Item> by RegHelper.itemSettings { fireResistant() } .withFlatModel()

    val TEST_ITEM: RegS<Item> by _TestItem.regHelper
    val REFINE_TEMP: RegS<Item> by Refinable.refineTemplate
    val WISH_ITEM: RegS<Item> by WishItem.regHelper
    val WISH_RESULT: RegS<Item> by WishItem.Result.regHelper
    val PRESCIENCE_MATRIX: RegS<Item> by PrescienceMatrix.regHelper

    val CURIO_REMOVER: RegS<Item> by CurioRemoverItem.regHelper
    val COSMIC_BIG_LOTTO: RegS<Item> by CosmicBigLotto.regHelper
}