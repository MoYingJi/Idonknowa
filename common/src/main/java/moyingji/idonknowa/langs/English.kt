package moyingji.idonknowa.langs

import moyingji.idonknowa.all.*
import moyingji.idonknowa.datagen.*
import moyingji.idonknowa.items.*
import moyingji.idonknowa.lang.*
import moyingji.lib.util.typed

object English : LangProvider {
    override val languageCode: String = "en_us"
    override fun genTranslations() {
        Translations.MOD_MENU_NAME to "Idonknowa"

        // region Items
        ModItem.PRIMOGEM tranTo "Primogem"
        ModItem.MORA tranTo "Mora"
        // endregion

        // region Blocks
        ModBlock.PRIMOGEM_ORE tranTo "Deepslate Primogem Ore"
        // endregion

        // region Classes
        ModItem.TEST_ITEM.value().typed<_TestItem>().apply {
            this tranTo "Test Item"
            refineName tranTo "Come Test"
            refineDesc tranLines """
                It's just a test item, even R@{level}
                I've heard that authors often pile up some unreleased features here
                Holding it in the off-hand can show you some detailed data about some events
            """.trimIndent()
        }
        ModItem.REFINE_TEMP.value().typed<ModSmithingTemplate>().builder.apply {
            key tranTo "Refine Template"
            appliesTo tranTo "Refinable Item"
            ingredients tranTo "Required Item"
            title tranTo "Refine Level Upgrade Smithing Template"
            baseDesc tranTo "Add items that need to be refined"
            additionsDesc tranTo "Add add-ons that can be added are generally the item itself"
        }
        ModItem.PRESCIENCE_MATRIX.value().typed<PrescienceMatrix>().apply {
            this tranTo "Matrix of Prescience"
        }
        // endregion
        
        // region Other
        ModTab.tranKey tranTo "Idonknowa"
        Translations.REFINE_LEVEL tranTo "Refine @{level}"
        Translations.PRESS_TO tranTo "Press [@{key}] to @{do}"
        Translations.DISPLAY_DETAILS tranTo "Display Details"
        // endregion
    }
}