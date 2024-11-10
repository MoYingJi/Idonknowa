package moyingji.idonknowa.datagen

import moyingji.idonknowa.recipe.ModRecipe
import moyingji.lib.util.forEachRemove
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.core.HolderLookup.Provider
import net.minecraft.data.recipes.RecipeOutput
import java.util.concurrent.CompletableFuture

class RecipeProvider(
    output: FabricDataOutput, lookup: CompletableFuture<Provider>
) : FabricRecipeProvider(output, lookup) {
    override fun buildRecipes(exporter: RecipeOutput) {
        ModRecipe.builders.forEachRemove { (b, i) ->
            b.save(exporter, i) }
    }
}