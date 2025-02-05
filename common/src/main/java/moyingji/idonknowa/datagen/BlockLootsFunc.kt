@file:Suppress("PackageDirectoryMismatch")
package moyingji.idonknowa.datagen.drop

import arrow.core.partially2
import moyingji.idonknowa.Idonknowa.isDatagen
import moyingji.idonknowa.autoreg.*
import moyingji.idonknowa.datagen.BlockLootProvider
import net.minecraft.block.Block
import net.minecraft.loot.LootTable

typealias BlockDPF = BlockLootProvider.(Block) -> Unit

private typealias BP = RSBlockProvider

fun BP.drop(f: BlockDPF): BP {
    isDatagen || return this
    return listen { BlockLootProvider.drops += f.partially2(it) }
}

fun BP.dropTable(
    f: LootTable.Builder.(Block) -> Unit
): BP = drop {
    val builder = LootTable.builder()
    f(builder, it)
    addDrop(it, builder)
}

val self: BlockDPF = { addDrop(it) }
val selfWithSilkTouch: BlockDPF = { addDropWithSilkTouch(it) }

fun drops(block: Block): BlockDPF = { addDrop(it, block) }
