package moyingji.idonknowa.recipe

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import moyingji.idonknowa.Id
import moyingji.idonknowa.Idonknowa.id
import moyingji.idonknowa.core.*
import moyingji.idonknowa.util.getId
import moyingji.lib.util.*
import net.minecraft.core.registries.Registries
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.crafting.*
import net.minecraft.world.level.ItemLike

class IdRecipeSerializer<R: Recipe<*>>(
    val getId: (R) -> Id = { ModRecipe.recipes
        .first { (r, _, _) -> it == r }.third },
    val getInstance: (Id) -> R = { ModRecipe.recipes
        .first { (_, _, i) -> it == i }.first.typed() },
) : RecipeSerializer<R> {
    constructor(r: SingleRecipe) : this( { r.id }, { r.instance.typed() } )
    override fun codec(): MapCodec<R> =  RecordCodecBuilder.mapCodec { it
        .group(Id.CODEC.fieldOf("id").forGetter(getId))
        .apply(it, getInstance) }
    override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, R>
        = StreamCodec.composite(Id.STREAM_CODEC, getId, getInstance)
    companion object {
        private val _regs: RegS<RecipeSerializer<Recipe<*>>>
            by RecipeSerializerRegHelper { IdRecipeSerializer() }
        val instance: RecipeSerializer<*> get() = _regs.value()
    }
}

class RecipeSerializerRegHelper<R: Recipe<*>>(
    initializer: () -> RecipeSerializer<R>
) : RegHelper<RecipeSerializer<R>>(Registries.RECIPE_SERIALIZER.typed(), initializer)

interface SingleRecipe {
    val instance: Recipe<*> get() = this.typed()
    val id: Id
}

fun RecipeBuilder.reg(id: Id) { ModRecipe.builders += this to id }
fun RecipeBuilder.reg(id: String) { reg(id.id) }
fun RecipeBuilder.regSingle(suffix: String? = null) {
    reg(result.getId()?.path?.let {
            if (suffix == null) it
            else it.suffix("_$suffix") }?.id
        ?: throw IllegalArgumentException()) }



typealias ItemRegS = RegS<out ItemLike>

fun <R: ShapedRecipeBuilder> R.define(symbol: Char, reg: ItemRegS): R = this.also { define(symbol, reg.value()) }
fun <R: ShapedRecipeBuilder> R.define(symbol: Char, vararg items: ItemLike): R = this.also { define(symbol, Ingredient.of(*items)) }