package moyingji.idonknowa

import com.mojang.logging.LogUtils
import dev.architectury.injectables.annotations.ExpectPlatform
import moyingji.idonknowa.all.ModItem
import moyingji.idonknowa.core.Events
import moyingji.idonknowa.datagen.*
import moyingji.idonknowa.lang.Translations
import moyingji.idonknowa.platform.*
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator.Pack
import org.slf4j.Logger

typealias platformed = ExpectPlatform

object Idonknowa {
    const val MOD_ID: String = "idonknowa"
    inline val logger: Logger get() = LogUtils.getLogger()

    var Loader = ModLoaderType.Unknown

    fun init() {
        ModLoaderType.check()

        ModItem // init

        Translations // init

        Events.default()
    }

    val isDatagen: Boolean = System.getProperty("fabric-api.datagen") != null
    fun datagen(gener: FabricDataGenerator) {
        val pack: Pack = gener.createPack()
        // Both
        LangProvider.gen(pack)
        // Client
        isClient() || return
        pack.addProvider(::ModelProvider)
    }
}
