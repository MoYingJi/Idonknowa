package moyingji.idonknowa.mixink

import com.google.common.collect.*
import com.google.common.collect.ImmutableMultimap.Builder
import moyingji.idonknowa.Id
import moyingji.idonknowa.recipe.ModRecipe
import net.minecraft.world.item.crafting.*

object RecipeManagerMixinImpl {
    var isApplied: Boolean = false; private set
    fun appendCustomInstance(
        byType: Builder<RecipeType<*>, RecipeHolder<*>>,
        byId: ImmutableMap.Builder<Id, RecipeHolder<*>>
    ) {
        ModRecipe.recipes.forEach { (recipe, type, id) ->
            val holder = RecipeHolder(id, recipe)
            byType.put(type, holder)
            byId.put(id, holder)
        }
        isApplied = true
    }
}