package moyingji.idonknowa.mia.impl

import dev.architectury.utils.GameInstance
import moyingji.idonknowa.mia.CIR
import moyingji.idonknowa.recipe.ModRecipe
import moyingji.lib.util.firstKeyOrNull
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipePropertySet

object RecipePropertySetMixinImpl {
    fun canUse(
        self: RecipePropertySet,
        stack: ItemStack,
        cir: CIR<Boolean>,
    ) {
        val server = GameInstance.getServer()!!
        val m = server.recipeManager
        val r = m.propertySets.firstKeyOrNull(self)
        val b = ModRecipe.recipeCanUse(r, self, stack)
        if (b != null) cir.returnValue = b
    }
}