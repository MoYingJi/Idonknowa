package moyingji.idonknowa.items

import moyingji.idonknowa.*
import moyingji.idonknowa.lang.TranKey
import net.minecraft.world.item.SmithingTemplateItem

open class ModSmithingTemplate(
    val builder: Builder,
) : SmithingTemplateItem(
    builder.appliesTo.text().withStyle(*builder.appliesFormatting.toTypedArray()),
    builder.ingredients.text().withStyle(*builder.ingredientsFormatting.toTypedArray()),
    builder.title.text().withStyle(*builder.titleFormatting.toTypedArray()),
    builder.baseDesc.text().withStyle(*builder.baseDescFormatting.toTypedArray()),
    builder.additionsDesc.text().withStyle(*builder.additionsDescFormatting.toTypedArray()),
    builder.baseIcon, builder.additionIcon
) {
    constructor(f: Builder.() -> Unit): this(Builder().also(f))
    override fun getDescriptionId(): String = builder.key.key

    class Builder {
        lateinit var key: TranKey

        val appliesFormatting: MutableList<Formatting> = mutableListOf()
        val ingredientsFormatting: MutableList<Formatting> = mutableListOf()
        val titleFormatting: MutableList<Formatting> = mutableListOf()
        val baseDescFormatting: MutableList<Formatting> = mutableListOf()
        val additionsDescFormatting: MutableList<Formatting> = mutableListOf()

        val baseIcon: MutableList<Id> = mutableListOf()
        val additionIcon: MutableList<Id> = mutableListOf()

        val appliesTo: TranKey get() = key.suffix("applies_to")
        val ingredients: TranKey get() = key.suffix("ingredients")
        val title: TranKey get() = key.suffix("title")
        val baseDesc: TranKey get() = key.suffix("base_slot_description")
        val additionsDesc: TranKey get() = key.suffix("additions_slot_description")

        fun defaultFormatting() {
            titleFormatting += Formatting.GRAY
            appliesFormatting += Formatting.BLUE
            ingredientsFormatting += Formatting.BLUE
        }
    }

    companion object {
        // 自动生成请勿编辑 (除非 bugjump 改了)
        val EMPTY_SLOT_HELMET: Id = Id.withDefaultNamespace("item/empty_armor_slot_helmet")
        val EMPTY_SLOT_CHESTPLATE: Id = Id.withDefaultNamespace("item/empty_armor_slot_chestplate")
        val EMPTY_SLOT_LEGGINGS: Id = Id.withDefaultNamespace("item/empty_armor_slot_leggings")
        val EMPTY_SLOT_BOOTS: Id = Id.withDefaultNamespace("item/empty_armor_slot_boots")
        val EMPTY_SLOT_HOE: Id = Id.withDefaultNamespace("item/empty_slot_hoe")
        val EMPTY_SLOT_AXE: Id = Id.withDefaultNamespace("item/empty_slot_axe")
        val EMPTY_SLOT_SWORD: Id = Id.withDefaultNamespace("item/empty_slot_sword")
        val EMPTY_SLOT_SHOVEL: Id = Id.withDefaultNamespace("item/empty_slot_shovel")
        val EMPTY_SLOT_PICKAXE: Id = Id.withDefaultNamespace("item/empty_slot_pickaxe")
        val EMPTY_SLOT_INGOT: Id = Id.withDefaultNamespace("item/empty_slot_ingot")
        val EMPTY_SLOT_REDSTONE_DUST: Id = Id.withDefaultNamespace("item/empty_slot_redstone_dust")
        val EMPTY_SLOT_QUARTZ: Id = Id.withDefaultNamespace("item/empty_slot_quartz")
        val EMPTY_SLOT_EMERALD: Id = Id.withDefaultNamespace("item/empty_slot_emerald")
        val EMPTY_SLOT_DIAMOND: Id = Id.withDefaultNamespace("item/empty_slot_diamond")
        val EMPTY_SLOT_LAPIS_LAZULI: Id = Id.withDefaultNamespace("item/empty_slot_lapis_lazuli")
        val EMPTY_SLOT_AMETHYST_SHARD: Id = Id.withDefaultNamespace("item/empty_slot_amethyst_shard")
        val EMPTY_SLOT_ALL: List<Id> = listOf(
            EMPTY_SLOT_HELMET, EMPTY_SLOT_CHESTPLATE, EMPTY_SLOT_LEGGINGS, EMPTY_SLOT_BOOTS,
            EMPTY_SLOT_HOE, EMPTY_SLOT_AXE, EMPTY_SLOT_SWORD, EMPTY_SLOT_SHOVEL,
            EMPTY_SLOT_PICKAXE, EMPTY_SLOT_INGOT, EMPTY_SLOT_REDSTONE_DUST, EMPTY_SLOT_QUARTZ,
            EMPTY_SLOT_EMERALD, EMPTY_SLOT_DIAMOND, EMPTY_SLOT_LAPIS_LAZULI, EMPTY_SLOT_AMETHYST_SHARD,
        )
    }
}