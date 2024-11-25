package moyingji.idonknowa.lang

import moyingji.idonknowa.*
import moyingji.idonknowa.datagen.CodeGen
import moyingji.lib.inspiration.TemplatedString
import net.minecraft.locale.Language
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block
import org.jetbrains.annotations.*
import java.io.File
import kotlin.reflect.KClass

typealias TranKey = TranslationKey

/**
 * 为区分字面符和翻译键 使用 [TranslationKey]
 * 附带很多功能
 */
class TranslationKey(val key: String) : ITransKey {
    override fun toString(): String = key

    @Contract("_ -> new")
    infix fun prefix(prefix: String): TranKey
    = TranKey("$prefix.$key")
    @Contract("_ -> new")
    infix fun suffix(suffix: String): TranKey
    = TranKey("$key.$suffix")
    @Contract("_ -> new")
    infix fun replacePrefix(prefix: String): TranKey
    = TranKey(prefix + "." + key.substringAfter('.'))
    @Contract("_ -> new")
    infix fun replaceSuffix(suffix: String): TranKey
    = TranKey(key.substringBeforeLast('.') + "." + suffix)

    // region Translation Values
    override val has: Boolean get() = language.has(key)
    override val value: String get() = language.getOrDefault(key)
    override val hasLines: Boolean get() = suffix("line0").has
    override val lines: String get() {
        val sb = StringBuilder()
        var index = 0
        while (suffix("line$index").has) {
            when (val value = suffix("line$index").value) {
                TranLinesSigns.EMPTY_LINE -> sb.appendLine()
                TranLinesSigns.EMPTY_STRING -> sb.append("")
                TranLinesSigns.BREAK -> break
                else -> sb.appendLine(value) }
            index ++ }
        if (sb.isEmpty()) sb.appendLine(suffix("lines").key)
        return sb.toString().substringBeforeLast("\n")
    }
    override fun text(vararg args: Any?): MutableText = Text.translatable(key, *args)
    // endregion

    // region 注释性方法
    @Suppress("NOTHING_TO_INLINE", "UNUSED_PARAMETER")
    inline fun withArgs(vararg arg: String): TranKey = this
    @Suppress("NOTHING_TO_INLINE", "UNUSED_PARAMETER")
    inline fun withArg(arg: String, what: String = ""): TranKey = this
    // endregion

    // region Translate to TranProvider
    var translate: TranProvider? = null
    override fun tranTo(@Nls value: String, provider: TranProvider?) {
        val p: TranProvider = provider ?: translate ?: globalTranslationProvider
        p.translate(key, value) }
    override fun tranLines(@Nls lines: String, provider: TranProvider?) {
        val p: TranProvider = provider ?: translate ?: globalTranslationProvider
        lines.lines().forEachIndexed { i, s ->
            p.translate("$key.line$i", s) } }
    // endregion
}

interface Translatable {
    fun tranTo(@Nls value: String, provider: TranProvider?)
    infix fun tranTo(@Nls value: String) { tranTo(value, null) }
    fun tranLines(@Nls lines: String, provider: TranProvider?)
    infix fun tranLines(@Nls lines: String) { tranLines(lines, null) }

    companion object {
        val EMPTY = object : Translatable {
            override fun tranTo(value: String, provider: TranProvider?) {}
            override fun tranTo(value: String) {}
            override fun tranLines(lines: String, provider: TranProvider?) {}
            override fun tranLines(lines: String) {}
        }
    }
}
interface TranslateValue {
    val templated: TemplatedString get() = TemplatedString(value)
    val templatedLines: TemplatedString get() = TemplatedString(lines)
    val has: Boolean
    val value: String
    val hasLines: Boolean
    val lines: String
    fun text(vararg args: Any?): MutableText = value
        .let { String.format(it, *args) }.text()
}

interface ITransKey : Translatable, TranslateValue

interface TranProvider { fun translate(key: String, value: String) }

val language: Language get() = Language.getInstance()
lateinit var globalTranslationProvider: TranProvider

object TranLinesSigns {
    const val EMPTY_LINE = "EMPTY_LINE"
    const val EMPTY_STRING = "EMPTY_STRING"
    const val BREAK = "BREAK"
}

class LazyTranslation : ITransKey {
    val translate: TranProvider? = null
    val actions: MutableList<() -> Unit> = mutableListOf()
    var key: TranKey? = null

    infix fun suffix(suffix: String): LazyTranslation = LazyTranslation()
        .also { t ->
            val action: () -> Unit = { key?.let { t.provideKey(it.suffix(suffix)) } }
            actions += action.also { it.invoke() }
        }

    override fun tranTo(value: String, provider: TranProvider?) {
        val g = globalTranslationProvider
        val action: () -> Unit = { key?.tranTo(value, provider ?: translate ?: g) }
        actions += action.also { it.invoke() }
    }
    override fun tranLines(lines: String, provider: TranProvider?) {
        val g = globalTranslationProvider
        val action: () -> Unit = { key?.tranLines(lines, provider ?: translate ?: g) }
        actions += action.also { it.invoke() }
    }

    fun provideKey(key: TranKey) {
        if (this.key != null) throw IllegalStateException("key is already provided")
        this.key = key
        actions.forEach { it.invoke() }
    }

    override val has: Boolean get() = key?.has == true
    override val value: String get() = key!!.value
    override val hasLines: Boolean get() = key?.hasLines == true
    override val lines: String get() = key!!.lines
    override fun text(vararg args: Any?): MutableText = key!!.text(*args)
}

fun String.tranKey(): TranKey = TranKey(this)
infix fun String.tranTo(value: String) { tranKey().tranTo(value) }
infix fun String.tranLines(lines: String) { tranKey().tranLines(lines) }
fun String.tranText(vararg args: Any?): MutableText = tranKey().text(*args)

fun genCode(codes: File) { CodeGen.genCodeIn("TranKeyExt", "moyingji.idonknowa.lang", codes) {
    appendLine("import moyingji.idonknowa.core.RegS")
    appendLine()
    for (clazz in genFs)
        appendLine("""
            infix fun ${clazz.qualifiedName}.tranTo(value: String) { tranKey().tranTo(value) }
            @JvmName("tran${clazz.simpleName}Key") fun RegS<out ${clazz.qualifiedName}>.tranKey(): TranKey = this.value().tranKey()
            @JvmName("tran${clazz.simpleName}To") infix fun RegS<out ${clazz.qualifiedName}>.tranTo(value: String) { tranKey().tranTo(value) }
            @JvmName("lazyKeySuffix${clazz.simpleName}") infix fun RegS<out ${clazz.qualifiedName}>.lazyKeySuffix(suffix: String): LazyTranslation = LazyTranslation().also { tran -> listen { tran.provideKey(it.tranKey()) } }
            infix fun ${clazz.qualifiedName}.lazyKeySuffix(suffix: String): Lazy<TranKey> = lazy { tranKey().suffix(suffix) }
        """.trimIndent()) } }
private val genFs: Array<KClass<*>> = arrayOf(
    ItemLike::class, Block::class
)
fun ItemLike.tranKey(): TranKey = this.asItem().descriptionId.tranKey()
fun Block.tranKey(): TranKey = this.descriptionId.tranKey()

