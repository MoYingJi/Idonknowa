package moyingji.idonknowa.mia.impl

import moyingji.idonknowa.Idonknowa
import moyingji.idonknowa.mia.CIR
import moyingji.idonknowa.recipe.ModRecipe
import moyingji.lib.util.firstKeyOf
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipePropertySet

object RecipePropertySetMixinImpl {
    fun canUse(
        self: RecipePropertySet,
        stack: ItemStack,
        cir: CIR<Boolean>,
    ) {
        val server = Idonknowa.server!!
        val r = server.recipeManager
            .propertySets.firstKeyOf(self)
        val b = ModRecipe.recipeCanUse(r, stack)
        if (b != null) cir.returnValue = b
    }
}