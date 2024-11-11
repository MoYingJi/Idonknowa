package moyingji.lib.collections

// region ListImpl (interface version of AbstractList?)
/**
 * 最大限度的实现了 [List]
 * 需要实现 [size] 和 [get]
 */
interface ListImpl<T> : List<T> {
    override val size: Int
    override fun get(index: Int): T

    override fun indexOf(element: T): Int = indexOfFirst { it == element }
    override fun lastIndexOf(element: T): Int = indexOfLast { it == element }
    override fun isEmpty(): Boolean = size == 0
    override fun contains(element: T): Boolean = any { it == element }
    override fun containsAll(elements: Collection<T>): Boolean = elements.all { contains(it) }
    override fun subList(fromIndex: Int, toIndex: Int): List<T> = SubList(this, fromIndex, toIndex - fromIndex)
    override fun iterator(): Iterator<T> = listIterator()
    override fun listIterator(): ListIterator<T> = listIterator(0)
    override fun listIterator(index: Int): ListIterator<T> = ListIteratorImpl(this, index)
}
/**
 * 最大限度的实现了 [MutableList]
 * 需要实现 [size] [get] [set] [add] [removeAt]
 * 仅提供基础功能
 */
interface MutableListImpl<T> : ListImpl<T>, MutableList<T> {
    override fun set(index: Int, element: T): T
    override fun add(index: Int, element: T)
    override fun removeAt(index: Int): T

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        for ((i, element) in elements.withIndex())
            add(index + i, element)
        return true
    }
    override fun remove(element: T): Boolean {
        val index = indexOf(element)
        if (index < 0) return false
        removeAt(index)
        return true
    }
    fun simpleBatchRemove(elements: Collection<T>, complement: Boolean): Boolean {
        var mod = false
        for (i in 0 until size)
            if (elements.contains(get(i)) != complement)
            { removeAt(i); mod = true }
        return mod
    }
    override fun clear() { while (!isEmpty()) removeAt(0) }
    override fun removeAll(elements: Collection<T>): Boolean = simpleBatchRemove(elements, false)
    override fun retainAll(elements: Collection<T>): Boolean = simpleBatchRemove(elements, true)

    override fun add(element: T): Boolean = true.also { add(size, element) }
    override fun addAll(elements: Collection<T>): Boolean = addAll(size, elements)
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = MutableSubList(this, fromIndex, toIndex - fromIndex)
    override fun iterator(): MutableIterator<T> = listIterator()
    override fun listIterator(): MutableListIterator<T> = listIterator(0)
    override fun listIterator(index: Int): MutableListIterator<T> = MutableListIteratorImpl(this, index)
}
// endregion

// region SubList
open class SubList<T>(open val base: List<T>, val offset: Int, override val size: Int) : ListImpl<T> {
    override fun get(index: Int): T = base[offset + index]
}
open class MutableSubList<T>(override val base: MutableList<T>, offset: Int, size: Int) : SubList<T>(base, offset, size),
                                                                                          MutableListImpl<T> {
    override fun set(index: Int, element: T): T = get(index).also { base[offset + index] = element }
    override fun add(index: Int, element: T) { base.add(offset + index, element) }
    override fun removeAt(index: Int): T = base.removeAt(offset + index)
    override fun addAll(index: Int, elements: Collection<T>): Boolean = base.addAll(offset + index, elements)
}
// endregion
