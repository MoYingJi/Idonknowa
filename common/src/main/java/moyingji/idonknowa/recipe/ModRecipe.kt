package moyingji.idonknowa.recipe

import moyingji.idonknowa.core.Regs
import moyingji.idonknowa.core.refine.refineData
import moyingji.idonknowa.util.id
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipePropertySet
import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier

object ModRecipe {
    val injectRecipes: MutableMap<Identifier, Recipe<*>> = mutableMapOf()

    init {
        injectRecipes += "refine_upgrade".id() to RefineUpSmithingRecipe
    }


    fun recipeCanUse(
        key: RegistryKey<RecipePropertySet>,
        stack: ItemStack
    ): Boolean? {
        if (key == RecipePropertySet.SMITHING_BASE)
            if (stack.refineData != null) return true
        if (Regs.REFINE.values.any { it.getUpgradeValue(stack) > 0 })
            return true
        return null
    }
}
