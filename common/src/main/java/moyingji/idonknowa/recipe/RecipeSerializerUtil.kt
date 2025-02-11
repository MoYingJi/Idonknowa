package moyingji.idonknowa.recipe

import com.mojang.serialization.MapCodec
import moyingji.idonknowa.autoreg.RSProvider
import moyingji.idonknowa.autoreg.RegS
import moyingji.idonknowa.serialization.RByteBuf
import moyingji.lib.util.firstKeyOf
import moyingji.lib.util.typed
import net.minecraft.network.codec.PacketCodec
import net.minecraft.recipe.*
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier

class RSRecipeSerializer<T: Recipe<*>>(
    provider: () -> RecipeSerializer<T>
) : RSProvider.Base<RecipeSerializer<T>>(
    RegistryKeys.RECIPE_SERIALIZER.typed(), provider
)

object RecipeSerializerUtil {
    val MOD_ID_SER: RegS<RecipeSerializer<Recipe<*>>>
        by RSRecipeSerializer(::RecipeModIdSerializer)
    fun <T: Recipe<*>> getRepSerByIdMod(): RecipeSerializer<T>
    = MOD_ID_SER.value().typed()
}

interface IRecipeIdSerializer<T: Recipe<*>> : RecipeSerializer<T> {
    override fun codec(): MapCodec<T>
    = Identifier.CODEC.xmap(::getRecipe, ::getId).fieldOf("id")

    @Deprecated("Deprecated in Java")
    override fun packetCodec(): PacketCodec<RByteBuf, T>
    = Identifier.PACKET_CODEC.xmap(::getRecipe, ::getId).mapBuf { it }

    fun getRecipe(id: Identifier): T
    fun getId(recipe: T): Identifier
}
class RecipeModIdSerializer : IRecipeIdSerializer<Recipe<*>> {
    override fun getRecipe(id: Identifier)
    : Recipe<*> = ModRecipe.injectRecipes[id]
        ?: throw IllegalArgumentException("No Recipe with id $id")
    override fun getId(recipe: Recipe<*>): Identifier
    = ModRecipe.injectRecipes.firstKeyOf(recipe)

    companion object {

    }
}
