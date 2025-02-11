package moyingji.idonknowa.core.refine

import moyingji.idonknowa.all.item.ModSmithingTemplate
import moyingji.idonknowa.datagen.lang.zh
import moyingji.idonknowa.lang.tran
import net.minecraft.item.ItemConvertible
import net.minecraft.util.Rarity

class RefineTempItem(
    settings: Settings
) : ModSmithingTemplate(
    Builder("item.idonknowa.refine_temp".tran()).apply {
        with (key) { zh to "精炼提升锻造模板" }
        with (appliesTo) { zh to "任何可精炼物品" }
        with (ingredients) { zh to "对应的消耗品" }
        with (baseDesc) { zh to "放入任意可精炼的物品" }
        with (additionsDesc) { zh to "放入此可精炼物品的对应消耗品" }
        baseIcon += ES_ALL
        additionIcon += ES_ALL
        defaultFormatting()
    }, settings.fireproof().rarity(Rarity.RARE)
) {
    companion object {
        val refinable: MutableList<ItemConvertible> = mutableListOf()
        val refine_addition: MutableList<ItemConvertible> = mutableListOf()
    }
}
