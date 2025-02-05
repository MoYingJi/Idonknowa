package moyingji.idonknowa.datagen

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
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
