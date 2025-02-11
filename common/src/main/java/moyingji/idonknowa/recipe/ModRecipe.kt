package moyingji.idonknowa.recipe

import moyingji.idonknowa.util.id
import net.minecraft.recipe.Recipe
import net.minecraft.util.Identifier

object ModRecipe {
    val injectRecipes: MutableMap<Identifier, Recipe<*>> = mutableMapOf()

    init {
        injectRecipes += "refine_upgrade".id() to RefineUpSmithingRecipe
    }
}
