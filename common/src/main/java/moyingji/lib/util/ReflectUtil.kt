package moyingji.lib.util

import java.lang.invoke.MethodHandles
import java.lang.reflect.*
import kotlin.reflect.*
import kotlin.reflect.jvm.isAccessible


fun <T: Any> KClass<T>.tryCreateInstance(vararg args: Any?): T?
= constructors.asSequence()
    .map { runCatching { it.call(*args) }.getOrNull() }
    .firstOrNull { it != null }


// region KProperty TypeAliases
// 泛型 V 代表属性类型 (返回值)
typealias KProp<V> = KProperty<V>
typealias KProp0<V> = KProperty0<V> // !Member & !Extension
typealias KProp1<R, V> = KProperty1<R, V> // Member | Extension
typealias KProp2<T, R, V> = KProperty2<T, R, V> // Member & Extension
typealias KPropWithoutReceiver<T> = KProp0<T>
typealias KPropWithReceiver<R, V> = KProp1<R, V>
typealias KPropWithMemberReceiver<T, R, V> = KProp2<T, R, V>
// endregion

fun <T: KCallable<*>> T.tryAccessible(
    finally: ((get: Result<Boolean>, set: Result<Unit>?) -> Unit)? = null,
): T = apply {
    val getAccessible: Result<Boolean> = runCatching { isAccessible }
    val setAccessible: Result<Unit>? = if (getAccessible.isFalse())
        runCatching { isAccessible = true } else null
    finally?.invoke(getAccessible, setAccessible)
}

// region getDelegate
inline fun <P: KProp<*>, reified D> P.typeDelegateNullableGetter(f: (P) -> Any?): D? {
    this.tryAccessible(); return this.let(f)?.typeNullable() }
inline fun <reified D> KProp0<*>.typeDelegateNullable()
: D? = typeDelegateNullableGetter { it.getDelegate() }
inline fun <R, reified D> KProp1<R, *>.typeDelegateNullable(receiver: R)
: D? = typeDelegateNullableGetter { it.getDelegate(receiver) }
fun <D> KProp0<*>.typeDelegate(): D = this.getDelegate().typed()
fun <R, D> KProp1<R, *>.typeDelegate(receiver: R): D = this.getDelegate(receiver).typed()

fun <T> KProp0<T>.delegateLazy(): Lazy<T>? = typeDelegateNullable()
fun <T> KProp0<T>.isLazyInited(): Boolean = this.delegateLazy()?.isInitialized() ?: true
// endregion



object ReflectUtil {
    fun <R> invokeMethod(receiver: Any, method: Method, args: Array<Any>): R
    = MethodHandles.lookup().unreflect(method).bindTo(receiver)
        .invokeWithArguments(*args).typed()

    val proxyAllowKtFunc: MutableSet<String>
    = mutableSetOf("invoke", "accept", "test")

    // region processFunction
    fun <T, F: Any> processFunction(
        clazz: KClass<out F>,
        fallback: Any? = null,
        allowName: Set<String> = emptySet(),
        fn: (invoker: (F) -> T) -> T
    ): F {
        val handler = InvocationHandler a@ {
            _, method, args ->
            if (method.name !in proxyAllowKtFunc && method.name !in allowName) {
                if (fallback == null) return@a null
                return@a invokeMethod(fallback, method, args) }
            val invoker: (F) -> T = { invokeMethod(it, method, args) }
            val r = fn(invoker); return@a r }
        val c = clazz.java
        val proxy = Proxy.newProxyInstance(
            this.javaClass.classLoader,
            if (c.isInterface) arrayOf(c) else c.interfaces,
            handler)
        return proxy.typed()
    }
    fun <T, F: Any> processFunction(
        fallback: Any? = null,
        allowName: Set<String> = emptySet(),
        vararg classGetter: F,
        fn: (invoker: (F) -> T) -> T
    ): F {
        require(classGetter.isEmpty())
        val clazz = classGetter.typeClassGetter()
        return processFunction(clazz, fallback, allowName, fn)
    }
    // endregion

    // region removeInvoker
    fun <F: Any> MutableCollection<F>.removeInvoker(
        r: Any? = Unit, allowName: Set<String> = emptySet()
    ): F = processFunction<Any?, F>(allowName) {
        this.forEachRemove { f -> it(f) }; r }
    fun <T, F: Any> MutableCollection<F>.removeInvoker(
        allowName: Set<String> = emptySet(), pr: (List<T>) -> T?
    ): F = processFunction<T?, F>(allowName) {
        val l: MutableList<T> = mutableListOf()
        this.forEachRemove { f ->
            it(f)?.also { l += it } }
        pr.invoke(l) }
    // endregion
}