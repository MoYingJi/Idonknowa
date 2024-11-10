package moyingji.lib.core

import org.intellij.lang.annotations.Flow
import kotlin.properties.*
import kotlin.reflect.KProperty

interface PropIn<T> : ReadWriteProperty<Any?, T>, PropRead<T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) { this.value = value }
    @set:Flow(sourceIsContainer = true) override var value: T
}
interface PropRead<T> : ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
    @get:Flow(targetIsContainer = true) val value: T
}

data class PropProvider<T>(
    override val value: T
) : PropRead<T>
data class PropValue<T>(
    override var value: T
) : PropIn<T>
class PropLazyProvider<T>(
    initializer: () -> T
) : PropRead<T> {
    override val value: T by lazy(initializer)
}

open class PropMap<A, T, R>(
    open val parent: ReadOnlyProperty<A, T>,
    val from: A.(T) -> R
) : ReadOnlyProperty<A, R> {
    override fun getValue(thisRef: A, property: KProperty<*>): R
    = from(thisRef, parent.getValue(thisRef, property))
}
class PropMutableMap<A, T, R>(
    override val parent: ReadWriteProperty<A, T>,
    from: A.(T) -> R,
    val to: A.(R) -> T
) : PropMap<A, T, R>(parent, from), ReadWriteProperty<A, R> {
    override fun setValue(thisRef: A, property: KProperty<*>, value: R)
    { parent.setValue(thisRef, property, to(thisRef, value)) }
}
@Suppress("UNUSED_PARAMETER")
object PropertyMap {
    fun <A, T, R> ReadOnlyProperty<A, T>.map(from: A.(T) -> R)
    : PropMap<A, T, R> = PropMap(this, from)
    fun <A, T, R> ReadOnlyProperty<A, T>.map(from: (T) -> R, unit: Unit = Unit)
    : PropMap<A, T, R> = PropMap(this) { from(it) }
    fun <A, T, R> ReadWriteProperty<A, T>.map(from: A.(T) -> R, to: A.(R) -> T)
    : PropMutableMap<A, T, R> = PropMutableMap(this, from, to)
    fun <A, T, R> ReadWriteProperty<A, T>.map(from: (T) -> R, to: (R) -> T, unit: Unit = Unit)
    : PropMutableMap<A, T, R> = PropMutableMap(this, { from(it) }, { to(it) })
}