package moyingji.idonknowa.datagen

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.client.data.*

class ModelProvider(output: FabricDataOutput) : FabricModelProvider(output) {
    companion object {
        val itemsF: MutableList<ItemModelGenerator.() -> Unit> = mutableListOf()
        val blocksF: MutableList<BlockStateModelGenerator.() -> Unit> = mutableListOf()
    }

    override fun generateItemModels(gener: ItemModelGenerator) {
        itemsF.forEach { it(gener) }
    }
    override fun generateBlockStateModels(gener: BlockStateModelGenerator) {
        blocksF.forEach { it(gener) }
    }
}
