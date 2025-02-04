package moyingji.idonknowa.autoreg

import arrow.core.partially1
import dev.architectury.registry.registries.*
import moyingji.idonknowa.Idonknowa
import moyingji.idonknowa.util.id
import moyingji.lib.util.*
import net.minecraft.registry.*
import net.minecraft.util.Identifier
import kotlin.reflect.KProperty

abstract class RSProvider<T>(
    val registry: RegistryKey<Registry<T>>,
    val namespace: String = Idonknowa.MOD_ID
) : PropReadDPA<RegS<T>> {
    init { require(Identifier.isNamespaceValid(namespace)) }

    open val registrar: Registrar<T> = RegistrarManager
        .get(namespace).get(registry)

    val actions: MutableList<(RegS<T>) -> Unit> = mutableListOf()
    infix fun action(f: (RegS<T>) -> Unit) { actions += f }

    abstract fun provide(id: Identifier): T

    override fun provideDelegate(thisRef: Any?, property: KProperty<*>)
    : PropReadA<RegS<T>> {
        val name = property.regName(String::lowercase)
        val id = name.id(namespace)
        val rs: RegS<T> = registrar.register(id, ::provide.partially1(id))
        actions.forEach { it(rs) }
        return PropReadA<RegS<T>> {
            _, _ -> rs }
    }

    open class Base<T>(
        reg: RegistryKey<Registry<T>>,
        namespace: String = Idonknowa.MOD_ID,
        val provider: () -> T = { throw NotImplementedError() },
    ) : RSProvider<T>(reg, namespace) {
        constructor(
            reg: RegistryKey<Registry<T>>,
            provider: () -> T
        ) : this(reg, Idonknowa.MOD_ID, provider)
        override fun provide(id: Identifier): T = provider()
    }

    open class SetKey<S, T>(
        reg: RegistryKey<Registry<T>>,
        val settings: S,
        val factory: (S) -> T,
        val keySet: (S, RegistryKey<T>) -> Unit
            = { _, _ -> },
        namespace: String = Idonknowa.MOD_ID,
    ) : RSProvider<T>(reg, namespace) {
        open fun keySet(settings: S, key: RegistryKey<T>)
        { keySet.invoke(settings, key) }
        override fun provide(id: Identifier): T {
            keySet(settings, RegistryKey.of(registry, id))
            return factory(settings)
        }
    }
}
