package moyingji.idonknowa.datagen

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.minecraft.core.HolderLookup.Provider
import net.minecraft.data.loot.BlockLootSubProvider
import java.util.concurrent.CompletableFuture

class BlockLootTableProvider(
    output: FabricDataOutput, lookup: CompletableFuture<Provider>
) : FabricBlockLootTableProvider(output, lookup) {
    override fun generate() {
        drops.removeAll { it(this); true }
    }
    companion object {
        val drops: MutableList<BlockLootSubProvider.() -> Unit> = mutableListOf()
    }
}



