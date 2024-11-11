package moyingji.lib.collections

import moyingji.lib.api.Mutable
import moyingji.lib.util.*

open class MapdCollection<T, R>(
    @Mutable open val parent: MutableCollection<T>,
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

open class MapdIterator<T, R>(
    @Mutable val parent: MutableIterator<T>,
    val from: (T) -> R
) : MutableIterator<R> {
    open fun from(element: T): R = from.invoke(element)
    override fun hasNext(): Boolean = parent.hasNext()
    override fun next(): R = parent.next().let(::from)
    override fun remove() { parent.remove() }
}

open class MapdMutableList<T, R>(
    @Mutable override val parent: MutableList<T>,
    from: (T) -> R, to: (R) -> T
) : MapdCollection<T, R>(parent, from, to), MutableList<R> {
    override fun add(index: Int, element: R) { parent.add(index, to(element)) }
    override fun addAll(index: Int, elements: Collection<R>): Boolean = parent.addAll(index, elements.map(::to))
    override fun get(index: Int): R = from(parent[index])
    override fun removeAt(index: Int): R = parent.removeAt(index).let(::from)
    override fun set(index: Int, element: R): R = parent.set(index, to(element)).let(::from)
    override fun lastIndexOf(element: R): Int = parent.lastIndexOf(to(element))
    override fun indexOf(element: R): Int = parent.indexOf(to(element))
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<R> = MutableSubList(this, fromIndex, toIndex - fromIndex)
    override fun listIterator(): MutableListIterator<R> = listIterator(0)
    override fun listIterator(index: Int): MutableListIterator<R> = MutableListIteratorImpl(this, index)
}


interface MapdCachable<T, R> { @Mutable val objectCache: MutableMap<T, R> }

open class MapdCachedCollection<T, R>(
    @Mutable parent: MutableCollection<T>,
    from: (T) -> R, to: (R) -> T,
    @Mutable override val objectCache: MutableMap<T, R> = mutableMapOf(),
    /** 函数 [to] 较为复杂时可以反向搜索缓存 */
    val toCached: Boolean = false
) : MapdCollection<T, R>(parent, from, to), MapdCachable<T, R> {
    override fun from(element: T): R = objectCache.getOrPut(element) { from.invoke(element) }
    override fun to(element: R): T = ifOrNull (toCached) {
        objectCache.firstKeyOfOrNull(element) } ?: toNoCache(element)
    fun toNoCache(element: R): T = to.invoke(element).also { objectCache += it to element }
    override fun iterator(): MutableIterator<R> = MapdCachedIterator(parent.iterator(), objectCache, ::from)
}

class MapdCachedIterator<T, R>(
    @Mutable parent: MutableIterator<T>,
    @Mutable override val objectCache: MutableMap<T, R> = mutableMapOf(),
    from: (T) -> R
) : MapdIterator<T, R>(parent, from), MapdCachable<T, R> {
    override fun from(element: T): R = objectCache.getOrPut(element) { from.invoke(element) }
}

class MapdCachedMutableList<T, R>(
    @Mutable parent: MutableList<T>,
    from: (T) -> R, to: (R) -> T,
    @Mutable override val objectCache: MutableMap<T, R> = mutableMapOf(),
    /** 函数 [to] 较为复杂时可以反向搜索缓存 */
    val toCached: Boolean = false
) : MapdMutableList<T, R>(parent, from, to), MapdCachable<T, R> {
    override fun from(element: T): R = objectCache.getOrPut(element) { from.invoke(element) }
    override fun to(element: R): T = ifOrNull (toCached) {
        objectCache.firstKeyOfOrNull(element) } ?: toNoCache(element)
    fun toNoCache(element: R): T = to.invoke(element).also { objectCache += it to element }
    override fun listIterator(index: Int): MutableListIterator<R> = MutableListIteratorImpl(this, index)
}