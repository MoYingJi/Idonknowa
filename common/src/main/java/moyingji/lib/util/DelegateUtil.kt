package moyingji.lib.util

import kotlin.properties.*
import kotlin.reflect.KProperty

typealias PropDPA<T> = PropertyDelegateProvider<Any?, T>
typealias PropReadA<T> = ReadOnlyProperty<Any?, T>
typealias PropReadDPA<T> = PropDPA<PropReadA<T>>

data class PropConst<T>(val value: T) : PropReadA<T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}
