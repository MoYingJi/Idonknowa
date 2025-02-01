package moyingji.idonknowa.datagen

import moyingji.idonknowa.*
import moyingji.idonknowa.core.RegHelper
import moyingji.idonknowa.datagen.ModModelProvider.blockModelProviders
import moyingji.idonknowa.datagen.ModModelProvider.itemModelProviders
import moyingji.lib.util.toOptional
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.client.data.models.*
import net.minecraft.client.data.models.model.*
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import java.util.*

// region Basic Class
class ModelProvider(output: FabricDataOutput) : FabricModelProvider(output) {
    override fun generateItemModels(gener: ItemModelGenerators) {
        itemModelProviders.removeAll { it.genItemModel(gener); true }
    }
    override fun generateBlockStateModels(gener: BlockModelGenerators) {
        blockModelProviders.removeAll { it.genBlockModel(gener); true }
    }
}
// 为避免调用 FabricModelProvider 故而单独定义
object ModModelProvider {
    val itemModelProviders: MutableList<ItemModelProvider> = mutableListOf()
    val blockModelProviders: MutableList<BlockModelProvider> = mutableListOf()
    fun whenReg(obj: Any?) { when (obj) {
        is ItemModelProvider -> itemModelProviders.add(obj)
        is BlockModelProvider -> blockModelProviders.add(obj)
    } }
}
interface ItemModelProvider {
    fun genItemModel(gener: ItemModelGenerators)
}
interface BlockModelProvider {
    fun genBlockModel(gener: BlockModelGenerators)
}
// endregion

// region RegHelper<Item>::withModel
@JvmName("withItemModel")
fun <I: Item, R: RegHelper<I>> R.withModel(
    provider: ItemModelGenerators.(I) -> Unit
): R = this.also {
    if (!Idonknowa.isDatagen) return@also
    listen { itemModelProviders += object : ItemModelProvider {
        override fun genItemModel(gener: ItemModelGenerators)
        { provider(gener, it) }
    } }
}

// region with only parent
@JvmName("withOnlyParentItemModel")
fun <I: Item, R: RegHelper<I>> R.withOnlyParent(parent: Id): R
= withModel { ModModelTemplates.create(parent).create(
    ModelLocationUtils.getModelLocation(it),
    TextureMapping(), this.modelOutput) }
@JvmName("withOnlyParentItemModel")
fun <I: Item, R: RegHelper<I>> R.withOnlyParent(parent: Item): R
= withOnlyParent(ModelLocationUtils.getModelLocation(parent))
@JvmName("withOnlyParentItemModel")
fun <I: Item, R: RegHelper<I>> R.withOnlyParent(parent: Block): R
= withOnlyParent(ModelLocationUtils.getModelLocation(parent))
// endregion

fun <I: Item, R: RegHelper<I>> R.withFlatModel(
    template: ModelTemplate = ModelTemplates.FLAT_ITEM
): R = withModel { generateFlatItem(it, template) }
fun <I: Item, R: RegHelper<I>> R.withFlatModel(
    texture: Item, template: ModelTemplate = ModelTemplates.FLAT_ITEM
): R = withModel { generateFlatItem(it, texture, template) }

// endregion

object ModModelTemplates {
    fun create(parent: Id, vararg slots: TextureSlot): ModelTemplate
    = ModelTemplate(parent.toOptional(), Optional.empty(), *slots)
}

// region RegHelper<Block>::withModel
@JvmName("withBlockModel")
fun <B: Block, R: RegHelper<B>> R.withModel(
    provider: BlockModelGenerators.(B) -> Unit
): R = listenIfDatagen { blockModelProviders += object : BlockModelProvider {
    override fun genBlockModel(gener: BlockModelGenerators)
    { provider(gener, it) }
} }

// region with only parent
@JvmName("withOnlyParentBlockModel")
fun <B: Block, R: RegHelper<B>> R.withOnlyParent(parent: Id): R
    = withModel { ModModelTemplates.create(parent).create(
    ModelLocationUtils.getModelLocation(it),
    TextureMapping(), this.modelOutput) }
@JvmName("withOnlyParentBlockModel")
fun <B: Block, R: RegHelper<B>> R.withOnlyParent(parent: Item): R
    = withOnlyParent(ModelLocationUtils.getModelLocation(parent))
@JvmName("withOnlyParentBlockModel")
fun <B: Block, R: RegHelper<B>> R.withOnlyParent(parent: Block): R
    = withOnlyParent(ModelLocationUtils.getModelLocation(parent))
// endregion

fun <B: Block, R: RegHelper<B>> R.withSimpleModel(
    provider: TexturedModel.Provider = TexturedModel.CUBE
): R = withModel { createTrivialBlock(it, provider) }

// endregion