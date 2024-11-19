package moyingji.idonknowa.serialization

import com.mojang.serialization.Codec
import moyingji.idonknowa.Id
import moyingji.idonknowa.Idonknowa.id
import moyingji.idonknowa.core.*
import moyingji.lib.api.autoName
import moyingji.lib.core.PropertyMap.map
import moyingji.lib.util.*
import net.minecraft.core.component.*
import net.minecraft.core.component.DataComponentType.Builder
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomModelData
import java.util.function.UnaryOperator
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.*

typealias NbtType<T> = DataComponentType<T>
typealias NbtTypeRegS<T> = RegS<NbtType<T>>

typealias NbtComp = CompoundTag


class NbtTypeRegHelper<T>(initializer: () -> NbtType<T>) : RegHelper<NbtType<T>>(Registries.DATA_COMPONENT_TYPE.typed(), initializer) {
    override fun provideId(prop: KProperty<*>): Id
    = prop.autoName("_") {
        it.lowercase().removeSuffix("_nbt") }.id
}

fun <T> nbtType(unaryOperator: UnaryOperator<Builder<T>>)
: NbtTypeRegHelper<T> = NbtTypeRegHelper { unaryOperator.apply(NbtType.builder()).build() }

fun <T> nbtType(codec: Codec<T>, streamCodec: SCodec<T>? = null): NbtTypeRegHelper<T>
= nbtType { it.persistent(codec).let { b -> if (streamCodec != null)
    b.networkSynchronized(streamCodec) else b } }

fun <T> nbtType(pair: Pair<Codec<T>, SCodec<T>?>): NbtTypeRegHelper<T>
= nbtType(pair.first, pair.second)

// region NbtProviders
interface NbtPropIn<T> { val type: NbtType<T> }

class NbtProp<T>(val _type: () -> NbtType<T>) : ReadWriteProperty<ItemStack, T?>, NbtPropIn<T> {
    override val type: NbtType<T> by lazy(_type)
    override fun getValue(thisRef: ItemStack, property: KProperty<*>): T? = thisRef.get(type)
    override fun setValue(thisRef: ItemStack, property: KProperty<*>, value: T?) { thisRef.set(type, value) }
    fun default(default: T & Any): NbtPropDefault<T> = NbtPropDefault(_type, default)
}
class NbtPropDefault<T>(type: () -> NbtType<T>, val default: T & Any) : ReadWriteProperty<ItemStack, T>, NbtPropIn<T> {
    override val type: NbtType<T> by lazy(type)
    override fun getValue(thisRef: ItemStack, property: KProperty<*>): T = thisRef.getOrDefault(type, default)
    override fun setValue(thisRef: ItemStack, property: KProperty<*>, value: T) { thisRef.set(type, value) }
}

fun <T> NbtType<T>.property(): NbtProp<T> = NbtProp { this }
fun <T> NbtTypeRegS<T>.property(): NbtProp<T> = NbtProp { this.value() }
fun <T> NbtType<T>.property(default: T & Any): NbtPropDefault<T> = NbtPropDefault({ this }, default)
fun <T> NbtTypeRegS<T>.property(default: T & Any): NbtPropDefault<T> = NbtPropDefault({ this.value() }, default)

@Throws(TypeCastException::class, IllegalPropertyDelegateAccessException::class)
fun <T> ItemStack?.getNbtPropType(prop: KProperty<T>): NbtType<T> = prop
    .typed<KPropWithReceiver<ItemStack, T>>().tryAccessible()
    .let {
        if (this == null) it.getExtensionDelegate()
        else it.getDelegate(this)
    }.typed<NbtPropIn<T>>().type
// endregion

var ItemStack.customModelData by DataComponents.CUSTOM_MODEL_DATA
    .property() map { it?.value } from { it?.let(::CustomModelData) }
var ItemStack.customData by DataComponents.CUSTOM_DATA.property()