package moyingji.idonknowa.datagen

import arrow.core.partially1
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator.Pack
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import java.util.concurrent.CompletableFuture

class LangProvider (
    val data: Data,
    output: FabricDataOutput,
    lookup: CompletableFuture<WrapperLookup>
) : FabricLanguageProvider(output, data.langCode, lookup) {
    class Data(val langCode: String) {
        val map: MutableMap<() -> String, () -> String> = mutableMapOf()
        init { reg.add(this) }
    }
    companion object {
        val reg: MutableList<Data> = mutableListOf()
        fun gen(pack: Pack) {
            reg.forEach { pack.addProvider(::LangProvider.partially1(it)) }
        }
    }

    object C {
        val zh = Data("zh_cn")
        val en = Data("en_us")
    }

    override fun generateTranslations(
        lookup: WrapperLookup,
        builder: TranslationBuilder
    ) {
        data.map.forEach { (k, v) -> builder.add(k(), v()) }
    }
}