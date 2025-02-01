package moyingji.idonknowa

import com.mojang.logging.LogUtils
import dev.architectury.injectables.annotations.ExpectPlatform
import moyingji.idonknowa.datagen.LangProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator.Pack
import org.slf4j.Logger

typealias platformed = ExpectPlatform

object Idonknowa {
    const val MOD_ID: String = "idonknowa"
    inline val logger: Logger get() = LogUtils.getLogger()

    fun init() {
    }

    val isDatagen: Boolean = System.getProperty("fabric-api.datagen") != null
    fun datagen(gener: FabricDataGenerator) {
        val pack: Pack = gener.createPack()
        LangProvider.gen(pack)
    }
}