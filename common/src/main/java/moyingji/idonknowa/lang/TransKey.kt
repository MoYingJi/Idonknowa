package moyingji.idonknowa.lang

import moyingji.idonknowa.Idonknowa.isDatagen
import moyingji.idonknowa.datagen.LangProvider
import moyingji.idonknowa.util.text
import moyingji.lib.inspiration.TemplatedString
import net.minecraft.item.Item
import net.minecraft.text.*
import net.minecraft.util.Language
import org.jetbrains.annotations.Contract
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class TransKey(val key: String) : TransFutureKey( { key } ),
    ITransKey, TransProp {
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

    fun pre(): TransProp = object : TransProp {
        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>
        ): TransKey = this@TransKey.prefix(property.name)
    }
    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ): TransKey = this.suffix(property.name)
}

typealias TransProp = ReadOnlyProperty<Any?, TransKey>

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

    fun LangProvider.Data.to(value: () -> String)
    { if (isDatagen) tran(this, value) }
    fun LangProvider.Data.to(value: String)
    { if (isDatagen) tran(this, value) }
    fun LangProvider.Data.toLines(value: Iterable<() -> String>)
    { if (isDatagen) tranLines(this, value) }
    fun LangProvider.Data.toLines(value: String)
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

fun Item.tran(f: Translatable.() -> Unit) { f(tran()) }

