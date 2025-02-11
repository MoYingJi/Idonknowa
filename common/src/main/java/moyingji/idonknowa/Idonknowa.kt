package moyingji.idonknowa

import com.mojang.logging.LogUtils
import moyingji.idonknowa.all.*
import moyingji.idonknowa.core.*
import moyingji.idonknowa.datagen.LangProvider
import moyingji.idonknowa.datagen.drop.BlockLootProvider
import moyingji.idonknowa.datagen.model.ModelProvider
import moyingji.idonknowa.datagen.tag.TagProvider
import moyingji.idonknowa.lang.Translations
import moyingji.idonknowa.platform.*
import moyingji.idonknowa.recipe.ModRecipe
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator.Pack
import org.slf4j.Logger

object Idonknowa {
    const val MOD_ID: String = "idonknowa"
    inline val logger: Logger get() = LogUtils.getLogger()

    var Loader = ModLoaderType.Unknown

    fun init() {
        ModLoaderType.check()

        ModItems // init
        ModBlocks // init

        Tags // init
        ModRecipe // init

        Translations // init

        Events.default()
    }

    val isDatagen: Boolean = System.getProperty("fabric-api.datagen") != null
    fun datagen(gener: FabricDataGenerator) {
        isDatagen || return
        val pack: Pack = gener.createPack()
        // Both
        pack.addProvider(::BlockLootProvider)
        TagProvider.gen(pack)
        LangProvider.gen(pack)
        // Client
        isClient() || return
        pack.addProvider(::ModelProvider)
    }
}
