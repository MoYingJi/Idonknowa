package moyingji.idonknowa.datagen

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.client.data.*

class ModelProvider(output: FabricDataOutput) : FabricModelProvider(output) {
    companion object {
        const val FROZEN_MESSAGE = "Model Provider is Frozen!"
        val itemsF: MutableList<ItemModelGenerator.() -> Unit>
        = mutableListOf(); get() = if (!isItemF) field else error(FROZEN_MESSAGE)
        val blocksF: MutableList<BlockStateModelGenerator.() -> Unit>
        = mutableListOf(); get() = if (!isItemF) field else error(FROZEN_MESSAGE)

        private var isItemF = false
        private var isBlockF = false
    }

    override fun generateItemModels(gener: ItemModelGenerator) {
        itemsF.forEach { it(gener) }
        isItemF = true
    }
    override fun generateBlockStateModels(gener: BlockStateModelGenerator) {
        blocksF.forEach { it(gener) }
        isBlockF = true
    }
}
