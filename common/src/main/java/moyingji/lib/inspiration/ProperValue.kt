package moyingji.lib.inspiration

import moyingji.lib.core.PropRead

class ProperValue<T>(
    var default: T,
    val type: ProperType<T>,
): PropRead<T> {
    enum class ModifierType { TIMES, PLUS }
    class ProperType<T>(
        val plus  : (T, T) -> T,
        val times : (T, T) -> T
    ) { companion object {
        val DOUBLE = ProperType<Double>(
            { a, b -> a + b },
            { a, b -> a * b }
        )
    } }
    data class Modifier<T>(
        val type: ModifierType,
        val value: T
    ) {
        val added: MutableList<ProperValue<T>> = mutableListOf()
        fun remove() { added.forEach { it.remove(this) } }
    }

    override val value: T get() {
        var value = default
        modifiers.sortBy { it.type.ordinal }
        for (modifier in modifiers) value = when (modifier.type) {
            ModifierType.PLUS -> type.plus(value, modifier.value)
            ModifierType.TIMES -> type.times(value, modifier.value)
        }
        return value
    }

    companion object {
        fun ofDouble(default: Double): ProperValue<Double>
        = ProperValue(default, ProperType.DOUBLE)
        fun ofDouble(default: Int): ProperValue<Double>
        = ofDouble(default.toDouble())
    }

    private val modifiers: MutableList<Modifier<T>> = mutableListOf()
    fun mod(mod: Modifier<T>) { modifiers += mod.also { it.added += this } }
    fun mod(type: ModifierType, value: T): Modifier<T>
    = Modifier(type, value).also { modifiers += it }
    fun remove(mod: Modifier<T>) { mod.remove(); mod.added -= this }

    operator fun plus(value: T): Modifier<T> = mod(ModifierType.PLUS, value)
    operator fun times(value: T): Modifier<T> = mod(ModifierType.TIMES, value)
}