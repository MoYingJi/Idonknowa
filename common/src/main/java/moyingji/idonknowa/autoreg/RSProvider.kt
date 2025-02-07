package moyingji.idonknowa.autoreg

import arrow.core.*
import dev.architectury.registry.registries.*
import moyingji.idonknowa.Idonknowa
import moyingji.idonknowa.util.id
import moyingji.lib.prop.*
import net.minecraft.registry.*
import net.minecraft.util.Identifier
import kotlin.reflect.KProperty

abstract class RSProvider<T>(
    val registry: RegistryKey<Registry<T>>,
    val namespace: String = Idonknowa.MOD_ID
) : PropNamed<RegS<T>> {
    init { require(Identifier.isNamespaceValid(namespace)) }

    open val registrar: Registrar<T> = RegistrarManager
        .get(namespace).get(registry)

    val actions: MutableList<(RegS<T>) -> Unit> = mutableListOf()
    infix fun action(f: (RegS<T>) -> Unit) {
        regs.fold( { f(it) }, { actions += f } )
    }

    abstract fun provide(id: Identifier): T

    fun register(id: Identifier): RegS<T> {
        require(regs.isRight())
        val rs: RegS<T> = registrar.register(id, ::provide.partially1(id))
        rs.listen { if (it is RSCallback) it.acceptRegistered() }
        regs = Either.Left(rs)
        actions.removeIf { it(rs); true }
        return rs
    }
    fun register(name: String): RegS<T> = register(name.id(namespace))

    override fun getValueFromName(name: String): RegS<T> = register(name)

    var regs: Either<RegS<T>, Unit> = Either.Right(Unit)
        protected set

    override fun provideDelegate(thisRef: Any?, property: KProperty<*>)
    : PropReadA<RegS<T>> {
        val name = property.propName(String::lowercase)
        return register(name).propConst()
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
