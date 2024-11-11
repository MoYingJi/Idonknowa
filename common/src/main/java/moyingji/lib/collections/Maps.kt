package moyingji.lib.collections

// region Default Map
interface HasDefaultValue<K, V> {
    fun defaultValue(key: K): V
}

interface DefaultMap<K, V> : Map<K, V>, HasDefaultValue<K, V> {
    override fun get(key: K): V
}
interface MutableDefaultMap<K, V> : MutableMap<K, V>, DefaultMap<K, V> {
    fun defaultValueAndPut(key: K): V = defaultValue(key)
        .also { put(key, it) }
    fun getAndPut(key: K): V = get(key)
        .also { if (!containsKey(key)) put(key, it) }
}

class DefaultMapImpl<K, V>(
    val map: Map<K, V> = mapOf(),
    val default: (key: K) -> V
) : DefaultMap<K, V>, Map<K, V> by map {
    override fun defaultValue(key: K): V = default(key)
    override fun get(key: K): V = map[key] ?: defaultValue(key)
}
class MutableDefaultMapImpl<K, V>(
    val map: MutableMap<K, V> = mutableMapOf(),
    val default: (key: K) -> V
) : MutableDefaultMap<K, V>, MutableMap<K, V> by map {
    override fun defaultValue(key: K): V = default(key)
    override fun get(key: K): V = map[key] ?: defaultValue(key)
}
// endregion

// region Maps Map
class MutableMapsMap<K1, K2, V>(
    val map: MutableMap<K1, MutableMap<K2, V>> = mutableMapOf(),
    val default: () -> MutableMap<K2, V> = ::mutableMapOf
) : MutableDefaultMap<K1, MutableMap<K2, V>>, MutableMap<K1, MutableMap<K2, V>> by map {
    override fun defaultValue(key: K1): MutableMap<K2, V> = default()
    override fun get(key: K1): MutableMap<K2, V> = map[key] ?: defaultValueAndPut(key)

    fun get(x: K1, y: K2): V? = get(x)[y]
    operator fun get(pair: Pair<K1, K2>): V? = get(pair.first, pair.second)
    fun set(x: K1, y: K2, value: V): V? = get(x).put(y, value)
    operator fun set(pair: Pair<K1, K2>, value: V): V? = set(pair.first, pair.second, value)
}
class MutableDefaultMapsMap<K1, K2, V>(
    val map: MutableMap<K1, MutableDefaultMap<K2, V>> = mutableMapOf(),
    val defaultMap: (default: (y: K2) -> V) -> MutableDefaultMap<K2, V> = { MutableDefaultMapImpl(default = it) },
    val defaultValue: (x: K1, y: K2) -> V,
) : MutableDefaultMap<K1, MutableDefaultMap<K2, V>>,
    MutableMap<K1, MutableDefaultMap<K2, V>> by map
{
    override fun defaultValue(key: K1): MutableDefaultMap<K2, V> = defaultMap { defaultValue(key, it) }
    override fun get(key: K1): MutableDefaultMap<K2, V> = map[key] ?: defaultValueAndPut(key)

    fun get(x: K1, y: K2): V = get(x)[y]
    operator fun get(pair: Pair<K1, K2>): V = get(pair.first, pair.second)
    fun set(x: K1, y: K2, value: V): V? = get(x).put(y, value)
    operator fun set(pair: Pair<K1, K2>, value: V): V? = set(pair.first, pair.second, value)
}
// endregion