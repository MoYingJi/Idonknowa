// 代码自动生成 请勿更改
// The code is automatically generated, do not change it.
@file:Suppress("PackageDirectoryMismatch")
package moyingji.idonknowa.datagen

import moyingji.idonknowa.DataPack
import moyingji.idonknowa.rs.tag.ModTag
import moyingji.lib.util.typed
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.core.HolderLookup.Provider
import java.util.concurrent.CompletableFuture

object TagsProvider { fun provide(pack: DataPack) {
    pack.addProvider(::ModItemTagProvider)
    pack.addProvider(::ModBlockTagProvider)
    pack.addProvider(::ModPoiTagProvider)
} }
class ModItemTagProvider(output: FabricDataOutput, lookup: CompletableFuture<Provider>) : net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.ItemTagProvider(output, lookup) {
    override fun addTags(wrapperLookup: Provider) {
        ModTag.tagKeys.keys().forEach { key ->
            if (!key.isFor(this.registryKey)) return@forEach
            ModTag.tagKeys[key].forEach { it(this.tag(key.typed())) } } }
}
class ModBlockTagProvider(output: FabricDataOutput, lookup: CompletableFuture<Provider>) : net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.BlockTagProvider(output, lookup) {
    override fun addTags(wrapperLookup: Provider) {
        ModTag.tagKeys.keys().forEach { key ->
            if (!key.isFor(this.registryKey)) return@forEach
            ModTag.tagKeys[key].forEach { it(this.tag(key.typed())) } } }
}
class ModPoiTagProvider(output: FabricDataOutput, lookup: CompletableFuture<Provider>) : net.minecraft.data.tags.PoiTypeTagsProvider(output, lookup) {
    override fun addTags(wrapperLookup: Provider) {
        ModTag.tagKeys.keys().forEach { key ->
            if (!key.isFor(this.registryKey)) return@forEach
            ModTag.tagKeys[key].forEach { it(this.tag(key.typed())) } } }
}
