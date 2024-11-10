package moyingji.idonknowa.datagen

import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.*
import net.minecraft.data.tags.PoiTypeTagsProvider
import java.io.File
import kotlin.reflect.KClass

object TagsProviderGener {
    val gen: MutableList<Pair<String, KClass<out net.minecraft.data.tags.TagsProvider<*>>>>
    = mutableListOf(
        // 基础
        "ModItemTagProvider" to ItemTagProvider::class,
        "ModBlockTagProvider" to BlockTagProvider::class,
        // 其他
        "ModPoiTagProvider" to PoiTypeTagsProvider::class,
    )

    fun genCode(codes: File) { CodeGen.genCodeIn(
        "TagsProviderGen",
        "moyingji.idonknowa.datagen",
        codes
    ) {
        appendLine("""
            import moyingji.idonknowa.DataPack
            import moyingji.idonknowa.tag.ModTag
            import moyingji.lib.util.typed
            import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
            import net.minecraft.core.HolderLookup.Provider
            import java.util.concurrent.CompletableFuture
        """.trimIndent())
        appendLine()
        appendLine("object TagsProvider { fun provide(pack: DataPack) {")
        for ((n, _) in gen) appendLine("    pack.addProvider(::$n)")
        appendLine("} }")
        for ((n, t) in gen) appendLine("""
            class $n(output: FabricDataOutput, lookup: CompletableFuture<Provider>) : ${t.qualifiedName}(output, lookup) {
                override fun addTags(wrapperLookup: Provider) {
                    ModTag.tagKeys.keys().forEach { key ->
                        if (!key.isFor(this.registryKey)) return@forEach
                        ModTag.tagKeys[key].forEach { it(this.tag(key.typed())) } } }
            }
        """.trimIndent())
    } }
}