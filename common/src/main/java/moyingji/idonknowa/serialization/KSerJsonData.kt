package moyingji.idonknowa.serialization

import com.google.gson.*
import com.mojang.serialization.JsonOps
import kotlinx.serialization.*
import moyingji.lib.api.*
import moyingji.lib.util.typed
import net.minecraft.core.HolderLookup.Provider
import net.minecraft.nbt.*
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.level.saveddata.SavedData
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.*
import kotlin.reflect.full.createInstance

@OptIn(InternalSerializationApi::class)
class KSerJsonData<T: Any>(
    val data: T,
    val serializer: KSerializer<T> = data::class.serializer().typed()
) : SavedData(), ReadOnlyProperty<Any?, T> {
    override fun save(tag: NbtComp, registries: Provider): NbtComp {
        if (data is State) data.beforeSave(tag, registries)
        val kjs = KJson.encodeToString(serializer, data)
        val json = JsonParser.parseString(kjs)
        val nbt = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json)
        if (nbt !is NbtComp) throw IllegalArgumentException()
        tag.merge(nbt)
        if (data is State) data.onSaved(tag, registries)
        return tag
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = data

    companion object {
        fun <T: Any> fromNbt(
            nbt: NbtComp, serializer: KSerializer<T>,
            dataFix: DataFixTypes? = null
        ): KSerJsonData<T> {
            val json = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, nbt)
            if (json !is JsonObject) throw IllegalArgumentException()
            val js = json.toString()
            val kj = KJson.parseToJsonElement(js)
            val data = KJson.decodeFromJsonElement(serializer, kj)
            if (data is State) data.onLoaded(nbt, dataFix)
            return KSerJsonData(data, serializer)
        }
        fun <T: Any> fromNbt(
            nbt: NbtComp, clazz: KClass<T>,
            dataFix: DataFixTypes? = null,
        ): KSerJsonData<T> = fromNbt(nbt, clazz.serializer(), dataFix)

        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        fun <T: Any> type(
            clazz: KClass<T>,
            dataFix: DataFixTypes? = null,
            creator: () -> T = { clazz.createInstance() }
        ): Factory<KSerJsonData<T>> {
            val serializer = clazz.serializer()
            val factory: (CompoundTag, Any) -> KSerJsonData<T> = {
                c, _ -> fromNbt(c, serializer, dataFix) }
            val dataCreator: () -> KSerJsonData<T> = {
                KSerJsonData(creator(), serializer) }
            return Factory(dataCreator, factory, dataFix)
        }
    }

    interface State {
        fun beforeSave(@Mutable tag: NbtComp, registries: Provider) {}
        fun onSaved(@Mutable tag: NbtComp, registries: Provider) {}
        fun onLoaded(@Immutable tag: NbtComp, dataFix: DataFixTypes?) {}
    }
}