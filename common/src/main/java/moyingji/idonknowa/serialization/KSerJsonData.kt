package moyingji.idonknowa.serialization

import com.google.gson.*
import com.mojang.serialization.JsonOps
import kotlinx.serialization.*
import moyingji.lib.util.*
import net.minecraft.core.HolderLookup.Provider
import net.minecraft.nbt.*
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.level.saveddata.SavedData
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.*

@OptIn(InternalSerializationApi::class)
class KSerJsonData<T: Any>(
    val data: T,
    val serializer: KSerializer<T> = data::class.serializer().typed()
) : SavedData(), ReadOnlyProperty<Any?, T> {
    override fun save(tag: NbtComp, registries: Provider): NbtComp {
        val kjs = KJson.encodeToString(serializer, data)
        val json = JsonParser.parseString(kjs)
        val nbt = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json)
        if (nbt !is NbtComp) throw IllegalArgumentException()
        return tag.merge(nbt)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = data

    companion object {
        fun <T: Any> fromNbt(
            nbt: NbtComp, serializer: KSerializer<T>
        ): KSerJsonData<T> {
            val json = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, nbt)
            if (json !is JsonObject) throw IllegalArgumentException()
            val js = json.toString()
            val kj = KJson.parseToJsonElement(js)
            val data = KJson.decodeFromJsonElement(serializer, kj)
            return KSerJsonData(data, serializer)
        }
        fun <T: Any> fromNbt(
            nbt: NbtComp, clazz: KClass<T>
        ): KSerJsonData<T> = fromNbt(nbt, clazz.serializer())

        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        fun <T: Any> type(
            clazz: KClass<T>,
            dataFix: DataFixTypes? = null,
            creator: () -> T = { clazz.tryCreateInstance()!! }
        ): Factory<KSerJsonData<T>> {
            val serializer = clazz.serializer()
            val factory: (CompoundTag, Any) -> KSerJsonData<T> = {
                c, _ -> fromNbt(c, serializer) }
            val dataCreator: () -> KSerJsonData<T> = {
                KSerJsonData(creator(), serializer) }
            return Factory(dataCreator, factory, dataFix)
        }
    }
}