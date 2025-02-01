package moyingji.idonknowa.fabric

import com.mojang.logging.LogUtils
import moyingji.idonknowa.Idonknowa
import net.fabricmc.api.*
import net.fabricmc.fabric.api.datagen.v1.*

class IdonknowaFabric : ModInitializer, ClientModInitializer, DataGeneratorEntrypoint {
    override fun onInitialize() {
        LogUtils.getLogger().info("Hello Idonknowa from Fabric!")
        Idonknowa.init()
    }

    override fun onInitializeClient() {}

    override fun onInitializeDataGenerator(gener: FabricDataGenerator) {
        LogUtils.getLogger().info("Hello Idonknowa from Fabric Datagen!")
        Idonknowa.datagen(gener)
    }
}