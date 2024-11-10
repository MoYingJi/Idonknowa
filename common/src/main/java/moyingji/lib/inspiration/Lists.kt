package moyingji.lib.inspiration

import moyingji.lib.api.Final

interface DefaultList<T> : List<T>, HasDefaultValue<Int, T> {
    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun defaultValue(index: Int): T
    override fun get(index: Int): T
}
interface MutableDefaultList<T> : MutableList<T>, DefaultList<T> {
    fun defaultValueAndSet(index: Int): T = defaultValue(index).also { set(index, it) }
    fun addDefault(index: Int = size): Boolean = true.also { add(index, defaultValue(index)) }
    @Final fun completeDefaultTo(indexExclusive: Int) { while (size < indexExclusive) addDefault() }
    fun getAndSet(index: Int): T = get(index).also {
        if (index < size) return@also
        while (this.size < index) addDefault()
        add(it) }
}

class DefaultListImpl<T>(
    val list: List<T> = listOf(),
    val default: (index: Int) -> T
) : DefaultList<T>, List<T> by list {
    override fun defaultValue(index: Int): T = default(index)
    override fun get(index: Int): T = list.getOrElse(index, default)
}
open class MutableDefaultListImpl<T>(
    val list: MutableList<T> = mutableListOf(),
    val default: (index: Int) -> T
) : MutableDefaultList<T>, MutableList<T> by list {
    override fun defaultValue(index: Int): T = default(index)
    override fun get(index: Int): T = list.getOrElse(index, default)

    override fun add(index: Int, element: T): Unit
    = if (index <= size) list.add(index, element) else {
        completeDefaultTo(index)
        list.add(element); Unit }
    override fun set(index: Int, element: T): T {
        if (index < size) return list.set(index, element)
        completeDefaultTo(index)
        list.add(element)
        return default(index)
    }
}

class MutableListsList<T>(
    list: MutableList<MutableList<T>> = mutableListOf(),
    default: () -> MutableList<T> = ::mutableListOf
) : MutableDefaultListImpl<MutableList<T>>(list, { default() }) {
    override fun get(index: Int): MutableList<T> = list.getOrElse(index, ::defaultValueAndSet)

    operator fun get(x: Int, y: Int): T = get(x)[y]
    fun getOrNull(x: Int, y: Int): T? = get(x).getOrNull(y)
    operator fun get(pair: Pair<Int, Int>): T = get(pair.first, pair.second)
    fun getOrNull(pair: Pair<Int, Int>): T? = getOrNull(pair.first, pair.second)
    operator fun set(x: Int, y: Int, value: T): T = get(x).set(y, value)
    operator fun set(pair: Pair<Int, Int>, value: T): T = set(pair.first, pair.second, value)
}

class MutableDefaultListsList<T>(
    list: MutableList<MutableDefaultList<T>> = mutableListOf(),
    defaultList: (default: (y: Int) -> T) -> MutableDefaultList<T> = { MutableDefaultListImpl(default = it) },
    defaultValue: (x: Int, y: Int) -> T,
) : MutableDefaultListImpl<MutableDefaultList<T>>(list, { index -> defaultList { defaultValue(index, it) } }) {
    override fun get(index: Int): MutableDefaultList<T> = list.getOrElse(index, ::defaultValueAndSet)

    operator fun get(x: Int, y: Int): T = get(x)[y]
    operator fun get(pair: Pair<Int, Int>): T = get(pair.first, pair.second)
    operator fun set(x: Int, y: Int, value: T): T = get(x).set(y, value)
    operator fun set(pair: Pair<Int, Int>, value: T): T = set(pair.first, pair.second, value)
}