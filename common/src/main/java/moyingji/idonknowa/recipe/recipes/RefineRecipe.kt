package moyingji.idonknowa.recipe.recipes

import moyingji.idonknowa.Id
import moyingji.idonknowa.Idonknowa.id
import moyingji.idonknowa.all.ModItem
import moyingji.idonknowa.core.*
import moyingji.idonknowa.recipe.*
import moyingji.idonknowa.util.isOf
import moyingji.lib.util.*
import net.minecraft.core.HolderLookup.Provider
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.*
import net.minecraft.world.level.Level

object RefineRecipe : SmithingRecipe, SingleRecipe {
    override val id: Id = "refine_upgrade".id
    override fun assemble(input: SmithingRecipeInput, provider: Provider): ItemStack {
        val baseStack = input.base
        val refinable = baseStack.item as? Refinable
            ?: return ItemStack.EMPTY
        val result = refinable.upgradeRefine(
            baseStack, input.addition, needsCopy = true)
        return result
    }
    override fun isTemplateIngredient(stack: ItemStack): Boolean
    = stack isOf ModItem.REFINE_TEMP
    override fun isBaseIngredient(stack: ItemStack): Boolean
    = stack.item.typeNullable<Refinable>()?.canBeRefineBase(stack) == true
    override fun isAdditionIngredient(stack: ItemStack): Boolean
    = Refinable.refinables.any { it.canBeRefineAddition(stack) }
    override fun matches(input: SmithingRecipeInput, level: Level): Boolean
    =      isTemplateIngredient(input.template)
        && isBaseIngredient    (input.base)
        && isAdditionIngredient(input.addition)
    override fun getResultItem(provider: Provider): ItemStack = ItemStack.EMPTY
    override fun getSerializer(): RecipeSerializer<*> = IdRecipeSerializer.instance
}