package moyingji.lib.inspiration

open class CounteredIterator<T>(
    val parent: Iterator<T>, val count: Int
): Iterator<T> {
    protected var left = count
    override fun hasNext(): Boolean = left > 0 && parent.hasNext()
    override fun next(): T = if (left <= 0)
        throw NoSuchElementException() else parent.also { left -- }.next()
}
class DefaultedCounteredIterator<T>(
    parent: Iterator<T>, count: Int,
    val default: () -> T
): CounteredIterator<T>(parent, count) {
    override fun hasNext(): Boolean = left > 0
    override fun next(): T
    = if (!hasNext()) throw NoSuchElementException() else { left --
        if (parent.hasNext()) parent.next() else default() }
}