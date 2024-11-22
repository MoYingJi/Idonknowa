package moyingji.lib.core

import moyingji.lib.api.ExpectFrom
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

fun <T> T.propIn(): PropIn<T> = PropValue(this)
fun <T> T.propProvider(): PropRead<T> = PropProvider(this)

open class PropMap<A, T, R>(
    open val parent: ReadOnlyProperty<A, T>,
    val to: A.(T) -> R
) : ReadOnlyProperty<A, R> {
    override fun getValue(thisRef: A, property: KProperty<*>): R
    = to(thisRef, parent.getValue(thisRef, property))
}
class PropMutableMap<A, T, R>(
    override val parent: ReadWriteProperty<A, T>,
    to: A.(T) -> R, val from: A.(R) -> T
) : PropMap<A, T, R>(parent, to), ReadWriteProperty<A, R> {
    override fun setValue(thisRef: A, property: KProperty<*>, value: R)
    { parent.setValue(thisRef, property, from(thisRef, value)) }
}
class PropReadMutableMap<A, T, R>(
    override val parent: ReadWriteProperty<A, T>, to: A.(T) -> R
) : PropMap<A, T, R>(parent, to), ExpectFrom<A.(R) -> T, PropMutableMap<A, T, R>> {
    override fun from(from: A.(R) -> T): PropMutableMap<A, T, R>
    = PropMutableMap(parent, to, from)
    @Suppress("UNUSED_PARAMETER")
    fun from(from: (R) -> T, unit: Unit = Unit): PropMutableMap<A, T, R>
    = PropMutableMap(parent, to) { from(it) }
}

@Suppress("UNUSED_PARAMETER")
object PropertyMap {
    infix fun <A, T, R> ReadOnlyProperty<A, T>.map(to: A.(T) -> R)
    : PropMap<A, T, R> = PropMap(this, to)
    fun <A, T, R> ReadOnlyProperty<A, T>.map(to: (T) -> R, unit: Unit = Unit)
    : PropMap<A, T, R> = PropMap(this) { to(it) }
    fun <A, T, R> ReadWriteProperty<A, T>.map(to: A.(T) -> R, from: A.(R) -> T)
    : PropMutableMap<A, T, R> = PropMutableMap(this, to, from)
    fun <A, T, R> ReadWriteProperty<A, T>.map(to: (T) -> R, from: (R) -> T, unit: Unit = Unit)
    : PropMutableMap<A, T, R> = PropMutableMap(this, { to(it) }, { from(it) })
    infix fun <A, T, R> ReadWriteProperty<A, T>.map(to: A.(T) -> R)
    : PropReadMutableMap<A, T, R> = PropReadMutableMap(this, to)
    fun <A, T, R> ReadWriteProperty<A, T>.map(to: (T) -> R, unit: Unit = Unit)
    : PropReadMutableMap<A, T, R> = PropReadMutableMap(this) { to(it) }
}