package moyingji.idonknowa.datagen

import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import moyingji.idonknowa.*
import moyingji.idonknowa.rs.loot.LCParams
import moyingji.idonknowa.rs.loot.LootBuilder
import moyingji.idonknowa.rs.loot.ModLoot
import moyingji.lib.util.*
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider
import net.minecraft.core.HolderLookup.Provider
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.LootTable.Builder
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import java.util.function.BiConsumer

object LootDataProviders {
    val providers: MutableMap<LCParams, LPC> = mutableMapOf()
    val builders: Multimap<LCParams, Pair<LootBuilder, Id>> = MultimapBuilder
        .hashKeys().arrayListValues().build()

    private fun getOrCreateProvider(p: LCParams, d: DataPack): LPC = providers
        .getOrPut(p) { { o: FabricDataOutput, l: CompFuture<Provider> ->
            LootDataProvider(o, l, p) }.also { d.addProvider(it) } }

    fun provide(pack: DataPack) {
        ModLoot.tableBuilders.removeAll {
            (b, i, p) ->
            getOrCreateProvider(p, pack)
            builders.put(p, b to i) }
        if (ModLoot.tableBuilders.isNotEmpty())
            Idonknowa.warn("Some LootTable is not put")
    }
}

typealias LPC = (FabricDataOutput, CompFuture<Provider>) -> LootDataProvider

class LootDataProvider(
    output: FabricDataOutput,
    lookup: CompFuture<Provider>,
    param: LCParams = LootContextParamSets.EMPTY
) : SimpleFabricLootTableProvider(output, lookup, param) {
    override fun generate(output: BiConsumer<ResourceKey<LootTable>, Builder>) {
        for ((b, i) in LootDataProviders.builders[lootContextType]) {
            val k = ResourceKey.create(Registries.LOOT_TABLE, i)
            output.accept(k, b)
        }
    }
}