package moyingji.lib.util

import kotlin.properties.*

typealias PropDPA<T> = PropertyDelegateProvider<Any?, T>
typealias PropReadA<T> = ReadOnlyProperty<Any?, T>
typealias PropReadDPA<T> = PropDPA<PropReadA<T>>