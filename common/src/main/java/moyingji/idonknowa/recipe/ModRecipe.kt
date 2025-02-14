package moyingji.idonknowa.recipe

import moyingji.idonknowa.core.Regs
import moyingji.idonknowa.core.refine.refineData
import moyingji.idonknowa.util.id
import net.minecraft.item.ItemStack
import net.minecraft.recipe.*
import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier

object ModRecipe {
    val injectRecipes: MutableMap<Identifier, Recipe<*>> = mutableMapOf()

    init {
        injectRecipes += "refine_upgrade".id() to RefineUpSmithingRecipe
    }


    fun recipeCanUse(
        key: RegistryKey<RecipePropertySet>?,
        set: RecipePropertySet,
        stack: ItemStack
    ): Boolean? {
        if (key == RecipePropertySet.SMITHING_BASE)
            if (stack.refineData != null)
                return true
        if (key == RecipePropertySet.SMITHING_ADDITION)
            if (Regs.REFINE.values.any { it.getUpgradeValue(stack) > 0u })
                return true
        return null
    }
}
