@file:Suppress("PackageDirectoryMismatch")
package moyingji.idonknowa.datagen.drop

import arrow.core.partially2
import moyingji.idonknowa.Idonknowa.isDatagen
import moyingji.idonknowa.autoreg.*
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.minecraft.block.Block
import net.minecraft.loot.LootTable
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

class BlockLootProvider(
    output: FabricDataOutput,
    lookup: CompletableFuture<RegistryWrapper.WrapperLookup>
) : FabricBlockLootTableProvider(output, lookup) {
    companion object {
        val drops: MutableList<BlockLootProvider.() -> Unit>
        = mutableListOf(); get() = if (!frozen) field
            else error("Block Loot Provider is Frozen!")
        private var frozen = false
    }

    override fun generate() {
        drops.forEach { it(this) }
        frozen = true
    }
}

typealias BlockDPF = BlockLootProvider.(Block) -> Unit

private typealias BP = RSBlockProvider

infix fun BP.drop(f: BlockDPF): BP {
    isDatagen || return this
    return listen { BlockLootProvider.drops += f.partially2(it) }
}

infix fun BP.dropTable(
    f: LootTable.Builder.(Block) -> Unit
): BP = drop {
    val builder = LootTable.builder()
    f(builder, it)
    addDrop(it, builder)
}

val self: BlockDPF = { addDrop(it) }
val selfWithSilkTouch: BlockDPF = { addDropWithSilkTouch(it) }

fun drops(block: Block): BlockDPF = { addDrop(it, block) }
