package moyingji.lib.prop

import moyingji.lib.util.findType
import kotlin.properties.*
import kotlin.reflect.*

typealias PropDPA<T> = PropertyDelegateProvider<Any?, T>
typealias PropReadA<T> = ReadOnlyProperty<Any?, T>
typealias PropReadDPA<T> = PropDPA<PropReadA<T>>

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class PropName(val name: String)

inline fun KCallable<*>.propName(
    conversion: (String) -> String = { it }
): String {
    val name: PropName? = this.annotations.findType()
    return name?.name ?: this.name.let(conversion)
}



data class PropConst<T>(val value: T) : PropReadA<T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}
fun <T> T.propConst(): PropConst<T> = PropConst(this)


fun interface PropNamed<T> : PropReadDPA<T> {
    fun getPropName(prop: KProperty<*>): String = prop.propName()
    override fun provideDelegate(
        thisRef: Any?, property: KProperty<*>
    ): PropReadA<T> {
        val name = getPropName(property)
        return getValueFromName(name).propConst()
    }
    fun getValueFromName(name: String): T
}
class PropConvNamed<T>(
    val conversion: (prop: String) -> String,
    val getter: (name: String) -> T,
) : PropNamed<T> {
    override fun getPropName(prop: KProperty<*>): String
    = super.getPropName(prop).let(conversion)
    override fun getValueFromName(name: String): T = getter(name)
}
fun <T> delegateName(getter: (name: String) -> T)
: PropReadDPA<T> = PropNamed(getter)
fun <T> delegateName(
    conversion: (prop: String) -> String,
    getter: (name: String) -> T
): PropReadDPA<T> = PropConvNamed(conversion, getter)
