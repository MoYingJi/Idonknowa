package moyingji.idonknowa.datagen

import moyingji.idonknowa.*
import moyingji.idonknowa.core.RegS
import moyingji.idonknowa.lang.*
import moyingji.lib.util.typed
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.core.HolderLookup
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import java.net.JarURLConnection
import java.util.jar.JarFile

typealias TranslationBuilder = FabricLanguageProvider.TranslationBuilder

object LangProviders {
    val providers: MutableList<LangProvider> = mutableListOf()
    init { runCatching { findAll("moyingji.idonknowa.langs") } }
    fun findAll(packageName: String) {
        val path = packageName.replace('.', '/')
        var jar: JarFile? = null
        Thread.currentThread()
            .contextClassLoader
            .getResources(path)
            .asIterator()
            .forEachRemaining { it -> try {
                jar = it.openConnection()
                    .typed<JarURLConnection>()
                    .jarFile
                (jar ?: return@forEachRemaining)
                    .entries().asIterator()
                    .forEachRemaining a@ {
                        if (it.isDirectory) return@a
                        if (!it.name.endsWith(".class")) return@a
                        if (!it.name.startsWith(path)) return@a
                        val name = it.name
                            .substringBeforeLast(".class")
                            .replace('/', '.')
                        val obj = Class.forName(name).kotlin
                            .objectInstance ?: return@a
                        if (obj is LangProvider && obj !in providers)
                            providers += obj.also {
                                Idonknowa.info("LangProviders found $name") }
                    }
            } catch (_: Exception) {} finally {
                runCatching { jar?.close() }
            } }
    }
    fun provide(pack: DataPack) { providers.removeAll {
        DataFactoryWithReg { o, r -> object : FabricLanguageProvider(o, it.languageCode, r) {
            override fun generateTranslations(lookup: HolderLookup.Provider, builder: TranslationBuilder) {
                val lang = TranBuilder(builder)
                globalTranslationProvider = lang
                it.genTranslations(lookup, builder)
                it.genTranslations(builder)
                it.genTranslations(lookup, lang)
                it.genTranslations(lang)
                it.genTranslations()
            }
        } }.let { pack.addProvider(it) }; true
    } }
}

interface LangProvider {
    val languageCode: String
    fun genTranslations(lookup: HolderLookup.Provider, builder: TranslationBuilder) {}
    fun genTranslations(builder: TranslationBuilder) {}
    fun genTranslations(lookup: HolderLookup.Provider, builder: TranBuilder) {}
    fun genTranslations(builder: TranBuilder) {}
    fun genTranslations() {}
}

class TranBuilder(val builder: TranslationBuilder) : TranProvider {
    override fun translate(key: String, value: String) { set(key, value) }

    @JvmName("setId")
    operator fun set(id: String, value: String)
    { builder.add(id, value) }
    @JvmName("setKey")
    operator fun set(key: TranslationKey, value: String)
    { builder.add(key.key, value) }

    @JvmName("setItem")
    operator fun set(item: RegS<Item>, value: String)
    { builder.add(item.value(), value) }
    @JvmName("setBlock")
    operator fun set(block: RegS<Block>, value: String)
    { builder.add(block.value(), value) }
}