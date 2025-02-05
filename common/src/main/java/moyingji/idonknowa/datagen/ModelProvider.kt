@file:Suppress("PackageDirectoryMismatch")
package moyingji.idonknowa.datagen.model

import moyingji.idonknowa.autoreg.BlockItemProviderData
import moyingji.idonknowa.autoreg.RSBlockProvider
import moyingji.idonknowa.autoreg.RSItemProvider
import moyingji.idonknowa.autoreg.listen
import moyingji.idonknowa.datagen.isDatagenClient
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.block.Block
import net.minecraft.client.data.*
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import kotlin.properties.Delegates

class ModelProvider(output: FabricDataOutput) : FabricModelProvider(output) {
    companion object {
        const val FROZEN_MESSAGE = "Model Provider is Frozen!"
        val itemsF: MutableList<ItemModelGenerator.() -> Unit>
        = mutableListOf(); get() = if (!isItemF) field else error(FROZEN_MESSAGE)
        val blocksF: MutableList<BlockStateModelGenerator.() -> Unit>
        = mutableListOf(); get() = if (!isItemF) field else error(FROZEN_MESSAGE)

        private var isItemF = false
        private var isBlockF = false
    }

    override fun generateItemModels(gener: ItemModelGenerator) {
        itemsF.forEach { it(gener) }
        isItemF = true
    }
    override fun generateBlockStateModels(gener: BlockStateModelGenerator) {
        blocksF.forEach { it(gener) }
        isBlockF = true
    }
}

// 为解决 服务端 / 客户端 冲突 暂用此方法

typealias ItemMRF = () -> ItemModelGenerator.(Item) -> Unit
typealias BlockMRF = () -> BlockStateModelGenerator.(Block) -> Unit

infix fun RSItemProvider.model(f: ItemMRF): RSItemProvider {
    isDatagenClient() || return this
    return listen { ModelProvider.itemsF += { f()(it) } }
}

infix fun RSBlockProvider.model(f: BlockMRF): RSBlockProvider {
    isDatagenClient() || return this
    return listen { ModelProvider.blocksF += { f()(it) } }
}
infix fun <T: BlockItemProviderData<*>> T.model(f: ItemMRF): T
        = apply { this.setter { model(f)} }

val generated: ItemMRF = { { this.register(it, Models.GENERATED) } }
val cubeAll: BlockMRF = { { this.registerSimpleCubeAll(it) } }

fun basic(parent: Identifier): ItemMRF = { { output.accept(it, ItemModels.basic(parent)) } }

val BlockItemProviderData<*>.blockItem: ItemMRF get() {
    var id: Identifier by Delegates.notNull()
    caller.listen { id = ModelIds.getBlockModelId(it) }
    return { { output.accept(it, ItemModels.basic(id)) } }
}
