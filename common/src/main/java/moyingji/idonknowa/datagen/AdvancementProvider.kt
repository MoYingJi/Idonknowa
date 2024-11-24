package moyingji.idonknowa.datagen

import moyingji.idonknowa.advancement.ModAdvancement
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.core.HolderLookup.Provider
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

class AdvancementProvider(output: FabricDataOutput, lookup: CompletableFuture<Provider>) : FabricAdvancementProvider(output, lookup) {
    override fun generateAdvancement(lookup: Provider, consumer: Consumer<AdvancementHolder>) {
        ModAdvancement.advancements.removeAll { consumer.accept(it); true }
    }
}