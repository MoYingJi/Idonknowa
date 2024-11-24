package moyingji.idonknowa.rs.loot

import moyingji.idonknowa.Idonknowa
import moyingji.idonknowa.core.*
import moyingji.idonknowa.datagen.BlockLootTableProvider.Companion.drops
import moyingji.idonknowa.datagen.listenIfDatagen
import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block

fun drop(drop: BlockLootSubProvider.() -> Unit) {
    if (Idonknowa.isDatagen) drops += drop }

fun <T, R: RegHelper<out T>> R.listenDrop(drop: BlockLootSubProvider.(T) -> Unit)
: R = listenIfDatagen { drop { drop(this, it) } }

fun <R: RegHelper<out Block>> R.dropSelf()
: R = listenDrop { dropSelf(it) }
fun <R: RegHelper<out Block>> R.dropOther(item: ItemLike)
: R = listenDrop { dropOther(it, item) }
fun <R: RegHelper<out Block>> R.dropOther(item: RegS<out ItemLike>)
: R = listenDrop { dropOther(it, item.value()) }

fun <R: RegHelper<out Block>> R.dropSilkAndOther(item: ItemLike)
: R = listenDrop { add(it, createSingleItemTableWithSilkTouch(it, item)) }
fun <R: RegHelper<out Block>> R.dropSilkAndOther(item: RegS<out ItemLike>)
: R = listenDrop { add(it, createSingleItemTableWithSilkTouch(it, item.value())) }