package moyingji.idonknowa.rs.loot

import moyingji.idonknowa.core.RegS
import net.minecraft.util.context.*
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.storage.loot.*
import net.minecraft.world.level.storage.loot.entries.*
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator.between

typealias LootBuilder = LootTable.Builder
typealias LootPoolBuilder = LootPool.Builder
typealias LootItemBuilder = LootPoolSingletonContainer.Builder<*>

typealias LootItemSetter = (@LootUtil LootItemBuilder).() -> Unit

@DslMarker @Target(AnnotationTarget.TYPE) annotation class LootUtil

fun lootBuilder(f: (@LootUtil LootBuilder).() -> Unit)
: LootBuilder = LootTable.lootTable().apply(f)

fun loot(f: (@LootUtil LootBuilder).() -> Unit)
: LootTable = lootBuilder(f).build()


fun LootBuilder.pool(f: (@LootUtil LootPoolBuilder).() -> Unit)
{ this.withPool(LootPool.lootPool().apply(f)) }


fun LootPoolBuilder.item(
    item: ItemLike,
    count: NumberProvider? = null, // = exactly(1)
    weight: Int? = null, // = LootPoolSingletonContainer.DEFAULT_WEIGHT
    f: LootItemSetter = {}
) { this.add(LootItem.lootTableItem(item).also {
    count?.apply { it.count(count) }; weight?.apply { it.weight(weight) }
}.apply(f)) }
fun LootPoolBuilder.item(
    item: RegS<out ItemLike>,
    count: NumberProvider? = null, weight: Int? = null,
    f: LootItemSetter = {}
) { this.item(item.value(), count, weight, f) }
fun LootPoolBuilder.rolls(number: NumberProvider) { this.setRolls(number) }
fun LootPoolBuilder.rolls(number: Number) { rolls(exactly(number)) }
fun LootPoolBuilder.rollsBetween(min: Number, max: Number) { rolls(between(min, max)) }


fun LootItemBuilder.count(number: NumberProvider) { this.apply(SetItemCountFunction.setCount(number)) }
fun LootItemBuilder.count(number: Number) { count(exactly(number)) }
fun LootItemBuilder.countBetween(min: Number, max: Number) { count(between(min, max)) }
fun LootItemBuilder.weight(weight: Int) { this.setWeight(weight) }


fun exactly(num: Number): NumberProvider = exactly(num.toFloat())
fun between(min: Number, max: Number): NumberProvider = between(min.toFloat(), max.toFloat())