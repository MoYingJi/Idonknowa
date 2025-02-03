@file:Suppress("PackageDirectoryMismatch")
package moyingji.idonknowa.datagen.models

import moyingji.idonknowa.autoreg.*
import moyingji.idonknowa.datagen.*
import moyingji.idonknowa.datagen.ModelProvider
import net.minecraft.block.Block
import net.minecraft.client.data.*
import net.minecraft.item.Item

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

val generated: ItemMRF = { { this.register(it, Models.GENERATED) } }
val cubeAll: BlockMRF = { { this.registerSimpleCubeAll(it) } }
