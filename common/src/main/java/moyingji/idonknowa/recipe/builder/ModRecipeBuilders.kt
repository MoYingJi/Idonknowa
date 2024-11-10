package moyingji.idonknowa.recipe.builder

import moyingji.idonknowa.core.RegS
import moyingji.idonknowa.util.getId
import moyingji.lib.util.typed
import net.minecraft.data.recipes.*
import net.minecraft.data.recipes.RecipeProvider.has
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.*
import net.minecraft.world.level.ItemLike


object ModRecipeBuilders {
    fun shaped(): ModShapedRecipeBuilder = ModShapedRecipeBuilder()
    fun cooking(): ModCookingRecipeBuilder = ModCookingRecipeBuilder()
}

fun <R: RecipeBuilder> R.unlockedByHas(item: ItemLike): R
= this.also { unlockedBy("has_${item.getId()?.path}", has(item)) }
fun <R: RecipeBuilder> R.unlockedByHas(reg: RegS<out ItemLike>): R = unlockedByHas(reg.value())
fun <R: RecipeBuilder> R.unlockedByHasResult(): R = unlockedByHas(result)

// region Shaped
class ModShapedRecipeBuilder {
    lateinit var result: ItemLike
    var count: Int = 1
    var category: RecipeCategory = RecipeCategory.MISC
    fun build(): ShapedRecipeBuilder = ShapedRecipeBuilder.shaped(
        category, result, count)
    fun result(item: ItemLike): ModShapedRecipeBuilder = this.also { result = item }
    fun result(reg: RegS<out ItemLike>): ModShapedRecipeBuilder = result(reg.value())
    fun count(count: Int): ModShapedRecipeBuilder = this.also { this.count = count }
    fun category(category: RecipeCategory): ModShapedRecipeBuilder = this.also { this.category = category }
}
// endregion

// region Cooking
typealias CookingRecipeType = Pair<
    RecipeSerializer<out AbstractCookingRecipe>,
    AbstractCookingRecipe.Factory<*>>
class ModCookingRecipeBuilder {
    lateinit var input: Ingredient
    lateinit var result: ItemLike
    var category: RecipeCategory = RecipeCategory.MISC
    var exp = 0F
    var time = 200
    var type: CookingRecipeType = CookingRecipeTypes.SMELTING
    fun build(): SimpleCookingRecipeBuilder = SimpleCookingRecipeBuilder.generic(
        input, category, result, exp, time, type.first, type.second.typed())

    fun input(ingredient: Ingredient): ModCookingRecipeBuilder = this.also { input = ingredient }
    fun result(item: ItemLike): ModCookingRecipeBuilder = this.also { result = item }
    fun category(category: RecipeCategory): ModCookingRecipeBuilder = this.also { this.category = category }
    fun exp(exp: Float): ModCookingRecipeBuilder = this.also { this.exp = exp }
    fun time(time: Int): ModCookingRecipeBuilder = this.also { this.time = time }
    fun type(type: CookingRecipeType): ModCookingRecipeBuilder = this.also { this.type = type }
    fun exp(exp: Number): ModCookingRecipeBuilder = this.also { this.exp = exp.toFloat() }

    fun input(item: ItemLike): ModCookingRecipeBuilder = input(Ingredient.of(item))
    fun input(reg: RegS<out ItemLike>): ModCookingRecipeBuilder = input(reg.value())
    fun result(reg: RegS<out ItemLike>): ModCookingRecipeBuilder = result(reg.value())
}
object CookingRecipeTypes {
    // 自动生成
    val SMELTING: CookingRecipeType = RecipeSerializer.SMELTING_RECIPE to AbstractCookingRecipe.Factory<AbstractCookingRecipe> { a: String, b: CookingBookCategory, c: Ingredient, d: ItemStack, e: Float, f: Int -> SmeltingRecipe(a, b, c, d, e, f) }
    val SMOKING : CookingRecipeType = RecipeSerializer.SMOKING_RECIPE  to AbstractCookingRecipe.Factory<AbstractCookingRecipe> { a: String, b: CookingBookCategory, c: Ingredient, d: ItemStack, e: Float, f: Int -> SmokingRecipe (a, b, c, d, e, f) }
    val BLASTING: CookingRecipeType = RecipeSerializer.BLASTING_RECIPE to AbstractCookingRecipe.Factory<AbstractCookingRecipe> { a: String, b: CookingBookCategory, c: Ingredient, d: ItemStack, e: Float, f: Int -> BlastingRecipe(a, b, c, d, e, f) }
}
// endregion