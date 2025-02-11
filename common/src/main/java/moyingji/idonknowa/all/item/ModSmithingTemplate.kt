package moyingji.idonknowa.all.item

import moyingji.idonknowa.lang.TransKey
import net.minecraft.item.SmithingTemplateItem
import net.minecraft.util.*

open class ModSmithingTemplate(
    builder: Builder, settings: Settings
) : SmithingTemplateItem(
    builder.appliesTo.text().formatted(*builder.appliesFormatting.toTypedArray()),
    builder.ingredients.text().formatted(*builder.ingredientsFormatting.toTypedArray()),
    builder.baseDesc.text().formatted(*builder.baseDescFormatting.toTypedArray()),
    builder.additionsDesc.text().formatted(*builder.additionsDescFormatting.toTypedArray()),
    builder.baseIcon, builder.additionIcon,
    settings.translationKey(builder.key.value)
) {
    class Builder(val key: TransKey) {
        val appliesFormatting: MutableList<Formatting> = mutableListOf()
        val ingredientsFormatting: MutableList<Formatting> = mutableListOf()
        val titleFormatting: MutableList<Formatting> = mutableListOf()
        val baseDescFormatting: MutableList<Formatting> = mutableListOf()
        val additionsDescFormatting: MutableList<Formatting> = mutableListOf()

        val baseIcon: MutableList<Identifier> = mutableListOf()
        val additionIcon: MutableList<Identifier> = mutableListOf()

        val appliesTo: TransKey get() = key.suffix("applies_to")
        val ingredients: TransKey get() = key.suffix("ingredients")
        val baseDesc: TransKey get() = key.suffix("base_slot_description")
        val additionsDesc: TransKey get() = key.suffix("additions_slot_description")

        fun defaultFormatting() {
            titleFormatting += Formatting.GRAY
            appliesFormatting += Formatting.BLUE
            ingredientsFormatting += Formatting.BLUE
        }
    }

    companion object {
        // 自动生成请勿编辑 (除非 bugjump 改了)
        fun emptySlot(name: String): Identifier = Identifier.ofVanilla("container/slot/$name")
        val ES_HELMET         : Identifier = emptySlot("helmet")
        val ES_CHESTPLATE     : Identifier = emptySlot("chestplate")
        val ES_LEGGINGS       : Identifier = emptySlot("leggings")
        val ES_BOOTS          : Identifier = emptySlot("boots")
        val ES_HOE            : Identifier = emptySlot("hoe")
        val ES_AXE            : Identifier = emptySlot("axe")
        val ES_SWORD          : Identifier = emptySlot("sword")
        val ES_SHOVEL         : Identifier = emptySlot("shovel")
        val ES_PICKAXE        : Identifier = emptySlot("pickaxe")
        val ES_INGOT          : Identifier = emptySlot("ingot")
        val ES_REDSTONE_DUST  : Identifier = emptySlot("redstone_dust")
        val ES_QUARTZ         : Identifier = emptySlot("quartz")
        val ES_EMERALD        : Identifier = emptySlot("emerald")
        val ES_DIAMOND        : Identifier = emptySlot("diamond")
        val ES_LAPIS_LAZULI   : Identifier = emptySlot("lapis_lazuli")
        val ES_AMETHYST_SHARD : Identifier = emptySlot("amethyst_shard")

        val ES_ALL: List<Identifier> = listOf(
            ES_HELMET, ES_CHESTPLATE, ES_LEGGINGS, ES_BOOTS,
            ES_HOE, ES_AXE, ES_SWORD, ES_SHOVEL,
            ES_PICKAXE, ES_INGOT, ES_REDSTONE_DUST, ES_QUARTZ,
            ES_EMERALD, ES_DIAMOND, ES_LAPIS_LAZULI, ES_AMETHYST_SHARD,
        )
    }
}
