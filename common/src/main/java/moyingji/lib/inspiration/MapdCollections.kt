package moyingji.lib.inspiration

import moyingji.lib.api.Mutable
import moyingji.lib.util.firstKeyOrNull

open class MapdCollection<T, R>(
    @Mutable val parent: MutableCollection<T>,
    // pure function to convert element
    val from: (T) -> R, val to: (R) -> T
) : MutableCollection<R> {
    open fun from(element: T): R = from.invoke(element)
    open fun to(element: R): T = to.invoke(element)

    override val size: Int get() = parent.size
    override fun clear() { parent.clear() }
    override fun isEmpty(): Boolean = parent.isEmpty()

    override fun contains(element: R): Boolean = parent.contains(to(element))
    override fun add(element: R): Boolean = parent.add(to(element))
    override fun remove(element: R): Boolean = parent.remove(to(element))

    override fun containsAll(elements: Collection<R>): Boolean = parent.containsAll(elements.map(::to).toSet())
    override fun retainAll(elements: Collection<R>): Boolean = parent.retainAll(elements.map(::to).toSet())
    override fun removeAll(elements: Collection<R>): Boolean = parent.removeAll(elements.map(::to).toSet())
    override fun addAll(elements: Collection<R>): Boolean = parent.addAll(elements.map(::to))

    override fun iterator(): MutableIterator<R> = MapdIterator(parent.iterator(), ::from)
}

class MapdIterator<T, R>(
    @Mutable val parent: MutableIterator<T>,
    val from: (T) -> R
) : MutableIterator<R> {
    fun from(element: T): R = from.invoke(element)
    override fun hasNext(): Boolean = parent.hasNext()
    override fun next(): R = parent.next().let(::from)
    override fun remove() { parent.remove() }
}

class MapdCachedCollection<T, R>(
    @Mutable parent: MutableCollection<T>,
    from: (T) -> R, to: (R) -> T
) : MapdCollection<T, R>(parent, from, to) {
    val objectCache: MutableMap<R, T> = mutableMapOf()

    override fun from(element: T): R = objectCache.firstKeyOrNull(element)
        ?: from.invoke(element).also { objectCache += it to element }
    override fun to(element: R): T = objectCache.getOrPut(element) { to.invoke(element) }
}