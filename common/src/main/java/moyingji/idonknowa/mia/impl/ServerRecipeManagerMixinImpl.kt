package moyingji.idonknowa.mia.impl

import moyingji.idonknowa.Idonknowa
import moyingji.idonknowa.mia.CIR
import moyingji.idonknowa.recipe.ModRecipe
import net.minecraft.recipe.*
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import java.util.*

object ServerRecipeManagerMixinImpl {
    fun beforeLoading(
        manager: ServerRecipeManager,
        rm: ResourceManager,
        profiler: Profiler,
        cir: CIR<PreparedRecipes>,
        map: SortedMap<Identifier, Recipe<*>>
    ) {
        map.putAll(ModRecipe.injectRecipes)
        Idonknowa.logger.info("Idonknowa Recipes Injected!")
    }
}
