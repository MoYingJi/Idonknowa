package moyingji.lib.api

import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.*
import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.*

typealias static = JvmStatic

// region Tools Method
@Suppress("UNUSED_PARAMETER")
inline fun <reified T: Annotation>
    KAnnotatedElement.tryFindAnno(
    clazz: KClass<T>? = null
) = runCatching { findAnnotation<T>() }
    .getOrNull()
inline fun <reified T: Annotation>
    KAnnotatedElement.tryFindAns(
) = runCatching { findAnnotations<T>() }
    .getOrNull() ?: listOf()
fun <T: Annotation>
    KAnnotatedElement.tryFindAns(
    clazz: KClass<T>
) = runCatching { findAnnotations(clazz) }
    .getOrNull() ?: listOf()
// endregion ToolsMethod

// region Reflection Annotations

/**
 * 表面该注解为供反射使用的注解 需要保留至 [RUNTIME] 时
 * 注解与否以及其值会影响部分代码的行为
 * 应严格按照注解给定的说明决定是否使用和参数值为何
 * 否则可能会出现意料之外的正常行为因为该行为取决于注解
 */
@Target(ANNOTATION_CLASS) @Retention(BINARY)
annotation class Reflection

/**
 * 万能注解 需要根据上下文推断其作用
 * 通常是让一些工具类的反射判断此注解并将其排除在作用之外
 */
@Reflection annotation class Except
val KAnnotatedElement.isExcepted
    get() = tryFindAnno<Except>() != null

/**
 * 万能注解 需要根据上下文推断其作用
 * 通常是让一些工具类的反射判断此注解并使用此名称而非字段或方法本身的名称
 */
@Reflection annotation class Name(val name: String)
val KAnnotatedElement.tagName
    get() = tryFindAnno<Name>()?.name
val KCallable<*>.autoNameWithoutPrefix
    get() = tagName ?: name
val KCallable<*>.autoName
    get() = propPrefix + autoNameWithoutPrefix
fun KCallable<*>.autoName(
    prefixConnector: String = "",
    case: (String) -> String
) = propPrefixOrNull
    .let { if (it == null) "" else it + prefixConnector } +
    (tagName ?: name.let { if (keepCase) it else case(it) })
fun KCallable<*>.autoName(
    case: (String) -> String
) = this.autoName("", case)

@Retention
annotation class PropPrefix(val prefix: String)
val KAnnotatedElement.propPrefixOrNull
    get() = tryFindAnno<PropPrefix>()?.prefix
val KAnnotatedElement.propPrefix
    get() = propPrefixOrNull ?: ""

@Retention annotation class KeepCase
val KAnnotatedElement.keepCase get()
= runCatching { hasAnnotation<KeepCase>() }.getOrNull() == true

// endregion Reflection Annotations

// region Source Annotations

/**
 * 对指定方法的返回值或形参或类中任意字段(getter)
 * 注明不要修改其值 即使其的类型是可被修改的!
 * 一般情况下因为其返回值每次调用都会重新生成或由 Mutable(List/Map) copy 而来
 * 对此进行修改有时不会产生任何效果 或产生意料之外的效果
 * 通常情况下会与 @(get:) [Contract] " -> new" 一起使用以指定修改其不产生任何效果
 * 不需要标注在原始类型中 不需要标注在已经注明为 Immutable 的类中
 * 具体看 [reason] (如果说明)
 */
@Retention(SOURCE) @MustBeDocumented
annotation class Immutable(val reason: String = "")

/**
 * 与 [Immutable] 相反 指定此值可被修改
 * 若为 Immutable 类型 你可以将其强转为 Mutable 类型不加以检查
 * (前提是调用者不违法此规则)
 * 若此已经是 Mutable 类型则用于强调其可以被修改或需要被修改
 * 具体看 [reason] (如果说明)
 */
@Retention(SOURCE) @MustBeDocumented
annotation class Mutable(val reason: String = "")

/** 一般情况下不能读取的值 可能仅用于某些特定用途 具体看 [reason] (如果说明) */
@Retention(SOURCE) @MustBeDocumented
annotation class Unreadable(val reason: String = "")

/**
 * 指定方法或字段(getter)为 [Final] 即使其 open
 * 通常用在接口中 指定不能重写此方法 此方法仅供调用
 * 否则会使接口失去原有作用 或产生意料之外的效果(一般不会)
 * 此类型的接口一般用于指定某类具有某种能力 通过该方法强制实现
 * 由于 final 关键字在 Kotlin 接口中不能使用 才有的这玩意
 * 不同于 [ApiStatus.NonExtendable] 的是
 * 这个没有警告(且这个更短) 还可以写原因
 * 具体看 [reason] (如果说明)
 */
@Retention(SOURCE) @MustBeDocumented
annotation class Final(val reason: String = "")

@Target(TYPE)
@Retention(SOURCE) @MustBeDocumented
annotation class OnceCall(val reason: String = "")

/**
 * 指定该接口仅能被某类实现
 * 一般情况下指定 KClass
 * 仅无法获取其 KClass 时使用 [target] 表述其类
 * 可以是泛型名称(若接口含泛型)、目标类泛型(若[clazz]含泛型)、类的签名等
 * 怎么说随你 能看懂就行
 * 对其子接口同样生效 (不用我说也知道)
 * (不遵守此规则可能抛出 [TypeCastException])
 * 若 [clazz] 和 [target] 同时填写则需要同时满足
 */
@Retention(SOURCE) @MustBeDocumented
annotation class OnlyCanBeImplementedBy(
    vararg val clazz: KClass<*> = [Any::class],
    @Language("kotlin", prefix = "class A : ") val target: String = "Any",
    val generics: Array<KClass<*>> = []
)

/**
 * 对于一个接口 指定其与某个 [clazz] 存在冲突
 * 具体看 [reason] (如果说明)
 */
@Retention(SOURCE) @MustBeDocumented
annotation class ConflictWith(val clazz: KClass<*>, val reason: String = "")

/**
 * 作为官方 @Range 注解的替代 具有更好的兼容性
 * 支持使用表达式 [condition] 对其进行精确具体的表达
 * 也支持使用 [from] .. [to] 进行范围表达
 * 使用 [Any] 和 [Nothing] 扩展了支持的类型
 * @see org.jetbrains.annotations.Range
 */
@Retention(SOURCE) annotation class Range(
    @Language("kotlin", prefix = "val condition: (Nothing) -> Boolean = { ", suffix = " }" )
    val condition: String = "true",
    @Language("kotlin", prefix = "val from: Any? get() = ")
    val from: String = "null",
    @Language("kotlin", prefix = "val to: Any? get() = ")
    val to: String = "null" )

// endregion Source Annotations

// region Source Annotation Method

/**
 * 表示前面的值同步至后面 不直接调用是因为此时调用后面的表达式会抛出错误
 * 在后面的表达式尚未初始化却要使用其值时迫不得已的一种方法 一个注释
 */
@Suppress("UNUSED_PARAMETER")
inline infix fun <T> T.syncFrom(valueFun: () -> T): T = this

// endregion Source Annotation Method