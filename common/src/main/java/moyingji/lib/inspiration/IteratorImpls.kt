package moyingji.lib.inspiration

interface IListIteratorImpl<T> : ListIterator<T> {
    val list: List<T>; var index: Int
    override fun hasNext(): Boolean = index < list.size
    override fun hasPrevious(): Boolean = index >= 0
    override fun next(): T = list[index++]
    override fun nextIndex(): Int = index
    override fun previous(): T = list[--index]
    override fun previousIndex(): Int = index - 1
}

interface IMutableListIteratorImpl<T> : IListIteratorImpl<T>, MutableListIterator<T> {
    var last: Int // default -1
    override val list: MutableList<T>
    override var index: Int
    override fun hasNext(): Boolean = index < list.size
    override fun next(): T = list[index++].also { last = index }
    override fun previous(): T = list[--index].also { last = index }
    override fun add(element: T) { list.add(nextIndex(), element); next() }
    override fun set(element: T) { list[nextIndex()] = element }
    override fun remove() { list.removeAt(last) }
}

open class ListIteratorImpl<T>(
    override val list: List<T>,
    override var index: Int = 0
) : IListIteratorImpl<T>

class MutableListIteratorImpl<T>(
    override val list: MutableList<T>,
    override var index: Int = 0
) : ListIteratorImpl<T>(list, index), IMutableListIteratorImpl<T> {
    override var last = -1
}