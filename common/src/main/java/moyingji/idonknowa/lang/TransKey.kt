package moyingji.idonknowa.lang

import moyingji.idonknowa.Idonknowa.isDatagen
import moyingji.idonknowa.datagen.LangProvider
import moyingji.idonknowa.util.text
import moyingji.lib.inspiration.TemplatedString
import moyingji.lib.util.*
import net.minecraft.item.Item
import net.minecraft.text.*
import net.minecraft.util.Language
import org.jetbrains.annotations.Contract
import kotlin.reflect.KProperty

class TransKey(val key: String) : TransFutureKey( { key } ),
    ITransKey, PropReadA<TransKey> {
    init {
        require(key.isNotBlank())
    }

    override fun toString(): String = key

    @Contract("_ -> new")
    infix fun suffix(suffix: String): TransKey
    = TransKey("$key.$suffix")
    @Contract("_ -> new")
    infix fun prefix(prefix: String): TransKey
    = TransKey("$prefix.$key")

    @Contract("_ -> new")
    infix fun replacePrefix(prefix: String): TransKey
    = TransKey(prefix + "." + key.substringAfter('.'))
    @Contract("_ -> new")
    infix fun replaceSuffix(suffix: String): TransKey
    = TransKey(key.substringBeforeLast('.') + "." + suffix)

    override val hasValue: Boolean
        get() = language.hasTranslation(key)
    override val hasLines: Boolean
        get() = language.hasTranslation("$key.line0")

    override val value: String get() = language.get(key)
    override val lines: String get() {
        val sb = StringBuilder()
        var index = 0
        while (this.suffix("line$index").hasValue) {
            when (val value = suffix("line$index").value) {
                TranLinesSigns.EMPTY_LINE -> sb.appendLine()
                TranLinesSigns.EMPTY_STRING -> {}
                TranLinesSigns.BREAK -> break
                else -> sb.appendLine(value) }
            index ++ }
        if (sb.isEmpty()) sb.appendLine(this.suffix("lines").key)
        return sb.toString().substringBeforeLast('\n')
    }

    override fun text(vararg args: Any?)
    : MutableText = Text.translatable(key, *args)

    // 以 prefix/suffix 方式 创建新 key 或分组

    fun pre(): PropReadA<TransKey> = PropReadA<TransKey> {
        _, property ->
        this@TransKey.prefix(property.name)
    }
    override fun getValue(
        thisRef: Any?, property: KProperty<*>
    ): TransKey = this.suffix(property.name)

    infix fun new(
        t: Translatable.() -> Unit = {}
    ): PropReadDPA<TransKey> = PropReadDPA<TransKey> {
        r, p ->
        val t = this@TransKey.getValue(r, p).apply(t)
        PropReadA<TransKey> { _, _ -> t } }
}

// region 注释性方法
@Suppress("NOTHING_TO_INLINE", "unused") inline fun <S: TranslateValue> S.args(vararg args: Pair<String, String>): S = this
@Suppress("NOTHING_TO_INLINE", "unused") inline fun <S: PropReadA<TranslateValue>> S.args(vararg args: Pair<String, String>): S = this
@Suppress("NOTHING_TO_INLINE", "unused") inline fun <S: PropReadDPA<TranslateValue>> S.args(vararg args: Pair<String, String>): S = this
// endregion

// 其他奇奇怪怪的东西

val language: Language get() = Language.getInstance()

object TranLinesSigns {
    const val EMPTY_LINE = "EMPTY_LINE"
    const val EMPTY_STRING = "EMPTY_STRING"
    const val BREAK = "BREAK"
}

interface ITransKey : TranslateValue, Translatable

interface TranslateValue {
    val hasValue: Boolean
    val hasLines: Boolean
    val value: String
    val lines: String
    val tempValue: TemplatedString get() = TemplatedString(value)
    val tempLines: TemplatedString get() = TemplatedString(lines)
    fun text(vararg args: Any?): MutableText = value
        .let { String.format(it, *args) }.text()
}

interface Translatable {
    fun tran(data: LangProvider.Data, value: () -> String)
    fun tranLines(data: LangProvider.Data, values: Iterable<() -> String>)

    fun tran(data: LangProvider.Data, value: String) {
        isDatagen || return
        val str = value.trim()
        require(!str.contains('\n'))
        tran(data) { value }
    }
    fun tranLines(data: LangProvider.Data, value: String) {
        if (isDatagen) tranLines(data, value.lines().map {{ it }})
    }

    infix fun LangProvider.Data.to(value: () -> String)
    { if (isDatagen) tran(this, value) }
    infix fun LangProvider.Data.to(value: String)
    { if (isDatagen) tran(this, value) }
    infix fun LangProvider.Data.toLines(value: Iterable<() -> String>)
    { if (isDatagen) tranLines(this, value) }
    infix fun LangProvider.Data.toLines(value: String)
    { if (isDatagen) tranLines(this, value) }
}

open class TransFutureKey(val keyFuture: () -> String) : Translatable {
    override fun tran(data: LangProvider.Data, value: () -> String) {
        if (isDatagen) data.map += keyFuture to value
    }
    override fun tranLines(
        data: LangProvider.Data,
        values: Iterable<() -> String>
    ) { isDatagen || return
        for ((i, line) in values.withIndex())
            data.map += { "${keyFuture()}.line$i" } to line }
}

fun String.tran(): TransKey = TransKey(this)

fun Item.tran(): Translatable = TransFutureKey { this.translationKey }

infix fun <I: Item> I.tran(f: Translatable.() -> Unit)
: I = this.also { f(tran()) }

