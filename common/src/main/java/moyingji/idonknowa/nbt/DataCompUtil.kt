package moyingji.idonknowa.nbt

import com.mojang.serialization.Codec
import moyingji.idonknowa.autoreg.*
import moyingji.idonknowa.serialization.*
import moyingji.lib.util.typed
import net.minecraft.component.ComponentType
import net.minecraft.item.ItemStack
import net.minecraft.registry.RegistryKeys
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

typealias DataCompType<T> = ComponentType<T>

typealias DataCompS<T> = RegS<DataCompType<T>>

class DataCompRSP<T>(
    provider: () -> DataCompType<T>
) : RSProvider.Base<DataCompType<T>>(
    RegistryKeys.DATA_COMPONENT_TYPE.typed(), provider
)

@Suppress("NOTHING_TO_INLINE", "unused")
inline fun <T> dataComp(
    noinline provider: () -> DataCompType<T>,
    unit: Unit = Unit // 辅助参数 避免编译器误报
): DataCompRSP<T> = DataCompRSP(provider)

fun <T> dataComp(
    builder: ComponentType.Builder<T>.() -> Unit
): DataCompRSP<T> = DataCompRSP {
    ComponentType.builder<T>().apply(builder).build()
}
fun <T> dataComp(
    codec: Codec<T>,
    pCodec: CoPRB<T>? = null
): DataCompRSP<T> = dataComp {
    codec(codec); if (pCodec != null) packetCodec(pCodec)
}
fun <T> dataComp(codecs: CoTup<T>): DataCompRSP<T>
= dataComp(codecs.first, codecs.second)


// region DataCompProp 属性委托获取 ItemStack 数据组件
fun <T> DataCompType<T>.prop(): DataCompProp<T>
= DataCompProp { this }
fun <T> DataCompType<T>.prop(default: ItemStack.() -> T): DataCompPropDefaulted<T>
= DataCompPropDefaulted( { this }, default)
fun <T> DataCompType<T>.prop(default: T): DataCompPropDefaulted<T>
= DataCompPropDefaulted( { this }, { default } )
fun <T> DataCompS<T>.prop(): DataCompProp<T>
= DataCompProp(::value)
fun <T> DataCompS<T>.prop(default: ItemStack.() -> T): DataCompPropDefaulted<T>
= DataCompPropDefaulted(::value, default)
fun <T> DataCompS<T>.prop(default: T): DataCompPropDefaulted<T>
= DataCompPropDefaulted(::value) { default }

class DataCompProp<T>(
    val comp: Lazy<DataCompType<T>>
) : ReadWriteProperty<ItemStack, T?> {
    override fun getValue(
        thisRef: ItemStack, property: KProperty<*>
    ): T? = thisRef.get(comp.value)
    override fun setValue(
        thisRef: ItemStack, property: KProperty<*>, value: T?
    ) { thisRef.set(comp.value, value) }

    constructor(f: () -> DataCompType<T>) : this( lazy { f() } )

    fun default(default: ItemStack.() -> T): DataCompPropDefaulted<T>
    = DataCompPropDefaulted(comp, default)
    fun default(default: T): DataCompPropDefaulted<T>
    = DataCompPropDefaulted(comp) { default }

    fun orThrow(
        default: ItemStack.() -> Nothing = { throw IllegalArgumentException() }
    ): DataCompPropDefaulted<T>
    = DataCompPropDefaulted(comp, default)
}
class DataCompPropDefaulted<T>(
    val comp: Lazy<DataCompType<T>>,
    val default: ItemStack.() -> T,
) : ReadWriteProperty<ItemStack, T> {
    override fun getValue(
        thisRef: ItemStack, property: KProperty<*>
    ): T = thisRef.get(comp.value) ?: default(thisRef)
    override fun setValue(
        thisRef: ItemStack, property: KProperty<*>, value: T
    ) { thisRef.set(comp.value, value) }

    constructor(
        f: () -> DataCompType<T>,
        default: ItemStack.() -> T
    ) : this(lazy { f() }, default)
}
// endregion
