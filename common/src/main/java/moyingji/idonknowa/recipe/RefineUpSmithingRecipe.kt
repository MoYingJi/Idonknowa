package moyingji.idonknowa.recipe

import moyingji.idonknowa.all.ModItems
import moyingji.idonknowa.core.refine.refinableItems
import moyingji.idonknowa.core.refine.refineData
import moyingji.lib.util.toOptional
import net.minecraft.item.ItemStack
import net.minecraft.recipe.*
import net.minecraft.recipe.input.SmithingRecipeInput
import net.minecraft.registry.RegistryWrapper
import net.minecraft.world.World
import java.util.*

object RefineUpSmithingRecipe : SmithingRecipe {
    override fun matches(
        input: SmithingRecipeInput, world: World
    ): Boolean {
        template.get().test(input.template) || return false
        val rd = input.base.refineData ?: return false
        rd.refine.maxRefineLevel < rd.level || return false
        val up = rd.refine.getUpgradeValue(input.addition)
        up > 0u || return false
        return true
    }

    override fun craft(
        input: SmithingRecipeInput,
        registries: RegistryWrapper.WrapperLookup
    ): ItemStack = input.base.refineData!!
        .refine.upgrade(input.base, input.addition)

    val template: Optional<Ingredient> by lazy { Ingredient
        .ofItem(ModItems.REFINE_TEMP.value()).toOptional() }
    val refinable: Optional<Ingredient> by lazy { Ingredient
        .ofItems(*refinableItems().toTypedArray()).toOptional() }

    override fun template(): Optional<Ingredient> = template
    override fun base(): Optional<Ingredient> = refinable
    override fun addition(): Optional<Ingredient> = refinable

    override fun getSerializer()
    : RecipeSerializer<out SmithingRecipe>
    = RecipeSerializerUtil.getRepSerByIdMod()

    override fun getIngredientPlacement(): IngredientPlacement = ingPlacement
    val ingPlacement: IngredientPlacement = IngredientPlacement
        .forMultipleSlots(listOf(template))
}
