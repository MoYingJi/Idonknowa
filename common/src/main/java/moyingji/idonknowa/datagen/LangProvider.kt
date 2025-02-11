package moyingji.idonknowa.datagen

import arrow.core.partially1
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator.Pack
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import java.util.concurrent.CompletableFuture

typealias LangData = LangProvider.Data

class LangProvider (
    val data: Data,
    output: FabricDataOutput,
    lookup: CompletableFuture<WrapperLookup>
) : FabricLanguageProvider(output, data.langCode, lookup) {
    class Data(val langCode: String) {
        val map: MutableMap<() -> String, () -> String>
        = mutableMapOf(); get() = if (!isFrozen) field
            else error(lazyMessage())

        var isFrozen = false; private set
        fun freeze() { isFrozen = true }
        val lazyMessage = {
            "LangProvider.Data ($langCode) is Frozen!"
        }

        init { reg.add(this) }

        operator fun set(key: () -> String, value: () -> String)
        { map[key] = value }
        operator fun plusAssign(pair: Pair<() -> String, () -> String>)
        { map += pair }
    }

    companion object {
        val reg: MutableList<Data> = mutableListOf()
        fun gen(pack: Pack) {
            reg.forEach { pack.addProvider(::LangProvider.partially1(it)) }
        }
    }

    override fun generateTranslations(
        lookup: WrapperLookup,
        builder: TranslationBuilder
    ) {
        for ((k, v) in data.map)
            builder.add(k(), v())
        data.freeze()
    }
}
