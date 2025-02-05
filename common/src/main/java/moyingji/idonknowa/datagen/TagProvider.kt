@file:Suppress("PackageDirectoryMismatch")
package moyingji.idonknowa.datagen.tag

import com.google.common.collect.*
import moyingji.idonknowa.Idonknowa.isDatagen
import moyingji.idonknowa.autoreg.*
import moyingji.idonknowa.datagen.tag.TagProvider.Companion.data
import moyingji.idonknowa.util.RegKeyOutReg
import moyingji.lib.util.typed
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator.Pack
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.registry.tag.TagKey
import java.util.concurrent.CompletableFuture

typealias TagBuilder<T> = FabricTagProvider<T>.FabricTagBuilder

class TagProvider<T>(
    val data: Data<T>,
    output: FabricDataOutput,
    lookup: CompletableFuture<WrapperLookup>
) : FabricTagProvider<T>(
    output, data.reg, lookup
) {
    companion object {
        val map: MutableMap<RegKeyOutReg<*>, Data<*>> = mutableMapOf()

        fun <T> data(registry: RegKeyOutReg<T>): Data<T>
        = map.getOrPut(registry) { Data(registry) }.typed()

        fun gen(pack: Pack) {
            for (d in map.values) pack.addProvider {
                o: FabricDataOutput, l: CompletableFuture<WrapperLookup> ->
                TagProvider(d, o, l)
            }
        }
    }

    class Data<T>(val reg: RegKeyOutReg<T>) {
        val map: Multimap<TagKey<T>, TagBuilder<T>.() -> Unit>
        = HashMultimap.create(); get() = if (!isFrozen) field
            else error(lazyMessage())

        var isFrozen = false; private set
        fun freeze() { isFrozen = true }
        val lazyMessage = {
            "TagProvider.Data (${reg.value}) is Frozen!"
        }
    }

    override fun configure(lookup: WrapperLookup) {
        isDatagen || return
        val map = data.map
        data.freeze()
        for (tag in map.keys()) {
            val b = getOrCreateTagBuilder(tag)
            map.get(tag).forEach { b.it() }
        }
    }
}

infix fun <T> TagKey<T>.tag(builder: TagBuilder<T>.() -> Unit) {
    isDatagen || return
    val d = data(registryRef)
    d.map.put(this, builder)
}

infix fun <T> T.tag(tag: TagKey<T>): T = also { tag.tag { add(it) } }
infix fun <S: RegS<T>, T> S.tag(tag: TagKey<T>): S = also { listen { it.tag(tag) } }
infix fun <P: RSProvider<T>, T> P.tag(tag: TagKey<T>): P = listen { it.tag(tag) }
