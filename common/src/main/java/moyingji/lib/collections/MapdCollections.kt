package moyingji.lib.collections

import moyingji.lib.api.Mutable
import moyingji.lib.util.*

open class MapdCollection<T, R>(
    @Mutable open val parent: MutableCollection<T>,
    // pure function to convert element
    val to: (T) -> R, val from: (R) -> T
) : MutableCollection<R> {
    open fun to(element: T): R = to.invoke(element)
    open fun from(element: R): T = from.invoke(element)

    override val size: Int get() = parent.size
    override fun clear() { parent.clear() }
    override fun isEmpty(): Boolean = parent.isEmpty()

    override fun contains(element: R): Boolean = parent.contains(from(element))
    override fun add(element: R): Boolean = parent.add(from(element))
    override fun remove(element: R): Boolean = parent.remove(from(element))

    override fun containsAll(elements: Collection<R>): Boolean = parent.containsAll(elements.map(::from).toSet())
    override fun retainAll(elements: Collection<R>): Boolean = parent.retainAll(elements.map(::from).toSet())
    override fun removeAll(elements: Collection<R>): Boolean = parent.removeAll(elements.map(::from).toSet())
    override fun addAll(elements: Collection<R>): Boolean = parent.addAll(elements.map(::from))

    override fun iterator(): MutableIterator<R> = MapdIterator(parent.iterator(), ::to)
}

open class MapdIterator<T, R>(
    @Mutable val parent: MutableIterator<T>,
    val to: (T) -> R
) : MutableIterator<R> {
    open fun to(element: T): R = to.invoke(element)
    override fun hasNext(): Boolean = parent.hasNext()
    override fun next(): R = parent.next().let(::to)
    override fun remove() { parent.remove() }
}

open class MapdMutableList<T, R>(
    @Mutable override val parent: MutableList<T>,
    to: (T) -> R, from: (R) -> T
) : MapdCollection<T, R>(parent, to, from), MutableList<R> {
    override fun add(index: Int, element: R) { parent.add(index, from(element)) }
    override fun addAll(index: Int, elements: Collection<R>): Boolean = parent.addAll(index, elements.map(::from))
    override fun get(index: Int): R = to(parent[index])
    override fun removeAt(index: Int): R = parent.removeAt(index).let(::to)
    override fun set(index: Int, element: R): R = parent.set(index, from(element)).let(::to)
    override fun lastIndexOf(element: R): Int = parent.lastIndexOf(from(element))
    override fun indexOf(element: R): Int = parent.indexOf(from(element))
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<R> = MutableSubList(this, fromIndex, toIndex - fromIndex)
    override fun listIterator(): MutableListIterator<R> = listIterator(0)
    override fun listIterator(index: Int): MutableListIterator<R> = MutableListIteratorImpl(this, index)
}


interface MapdCachable<T, R> { @Mutable val objectCache: MutableMap<T, R> }

open class MapdCachedCollection<T, R>(
    @Mutable parent: MutableCollection<T>,
    to: (T) -> R, from: (R) -> T,
    @Mutable override val objectCache: MutableMap<T, R> = mutableMapOf(),
    /** 函数 [from] 较为复杂时可以反向搜索缓存 */
    val toCached: Boolean = false
) : MapdCollection<T, R>(parent, to, from), MapdCachable<T, R> {
    override fun to(element: T): R = objectCache.getOrPut(element) { to.invoke(element) }
    override fun from(element: R): T = ifOrNull (toCached) {
        objectCache.firstKeyOfOrNull(element) } ?: toNoCache(element)
    fun toNoCache(element: R): T = from.invoke(element).also { objectCache += it to element }
    override fun iterator(): MutableIterator<R> = MapdCachedIterator(parent.iterator(), objectCache, ::to)
}

class MapdCachedIterator<T, R>(
    @Mutable parent: MutableIterator<T>,
    @Mutable override val objectCache: MutableMap<T, R> = mutableMapOf(),
    to: (T) -> R
) : MapdIterator<T, R>(parent, to), MapdCachable<T, R> {
    override fun to(element: T): R = objectCache.getOrPut(element) { to.invoke(element) }
}

class MapdCachedMutableList<T, R>(
    @Mutable parent: MutableList<T>,
    to: (T) -> R, from: (R) -> T,
    @Mutable override val objectCache: MutableMap<T, R> = mutableMapOf(),
    /** 函数 [from] 较为复杂时可以反向搜索缓存 */
    val toCached: Boolean = false
) : MapdMutableList<T, R>(parent, to, from), MapdCachable<T, R> {
    override fun to(element: T): R = objectCache.getOrPut(element) { to.invoke(element) }
    override fun from(element: R): T = ifOrNull (toCached) {
        objectCache.firstKeyOfOrNull(element) } ?: toNoCache(element)
    fun toNoCache(element: R): T = from.invoke(element).also { objectCache += it to element }
    override fun listIterator(index: Int): MutableListIterator<R> = MutableListIteratorImpl(this, index)
}