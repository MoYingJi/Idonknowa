package moyingji.idonknowa.lang

import moyingji.idonknowa.Idonknowa.isDatagen
import moyingji.idonknowa.datagen.LangData
import moyingji.idonknowa.util.*
import moyingji.lib.inspiration.TemplatedString
import moyingji.lib.prop.*
import net.minecraft.item.ItemConvertible
import net.minecraft.text.*
import net.minecraft.util.Language
import org.jetbrains.annotations.Contract
import kotlin.reflect.KProperty

data class TransKey(
    override val key: String
) : TransKeyCall( { key } ), ITransKey, PropReadA<TransKey> {
    init {
        require(key.isNotBlank())
    }

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
        t.propConst() }
}

// region 注释性方法
@Suppress("NOTHING_TO_INLINE", "unused") inline fun <S: TranslateValue> S.args(vararg args: Pair<String, String>): S = this
@Suppress("NOTHING_TO_INLINE", "unused") inline fun <S: PropReadDPA<TranslateValue>> S.args(vararg args: Pair<String, String>): S = this
@Suppress("NOTHING_TO_INLINE", "unused") inline fun <S: TranslateValue> S.multilines(): S = this
@Suppress("NOTHING_TO_INLINE", "unused") inline fun <S: PropReadDPA<TranslateValue>> S.multilines(): S = this
// endregion

// 其他奇奇怪怪的东西

val language: Language get() = Language.getInstance()

interface ITransKey : TranslateValue, Translatable {
    val key: String

    override val hasValue: Boolean
        get() = language.hasTranslation(key)
    override val value: String
        get() = language.get(key)

    override fun text(vararg args: Any?)
    : MutableText = Text.translatable(key, *args)
}

interface TranslateValue {
    val hasValue: Boolean
    val value: String
    val tempValue: TemplatedString get() = TemplatedString(value)
    fun text(vararg args: Any?): MutableText = value
        .let { String.format(it, *args) }.text()
}

interface Translatable {
    fun tran(data: LangData, value: () -> String)

    fun tran(data: LangData, value: String) {
        isDatagen || return
        tran(data) { value }
    }

    infix fun LangData.to(value: () -> String): LangData
    = apply { if (isDatagen) tran(this, value) }
    infix fun LangData.to(value: String): LangData
    = apply { if (isDatagen) tran(this, value) }
}

// region 两种无需立刻获取 key 的 Translatable 实现

open class TransKeyCall(val keyCall: () -> String) : Translatable {
    override fun tran(data: LangData, value: () -> String) {
        if (isDatagen) data += keyCall to value
    }

    // region itemAutoDesc / 仅限 ItemConvertible 的自动描述!
    fun LangData.itemAutoDesc(
        shift: Boolean, at: String, lines: String
    ): LangData = apply { if (isDatagen) { TransKeyCall {
        TooltipUtil.getAutoDescKey(keyCall().tran(), shift, at).key
    }.tran(this, lines) } }
    infix fun LangData.itemAutoDesc(lines: String): LangData
    = apply { itemAutoDesc(true, "before", lines) }
    // endregion
}

class TransLazyKey(val keyLazy: Lazy<String>): ITransKey {
    override fun tran(data: LangData, value: () -> String) {
        if (isDatagen) data += keyLazy::value to value
    }
    constructor(keyCall: () -> String) : this( lazy { keyCall() } )
    override val key: String by keyLazy
}

class TransAcceptedKey() : Translatable {
    val acceptors: MutableList<(key: Translatable) -> Unit> = mutableListOf()
    val keys: MutableList<Translatable> = mutableListOf()
    fun accept(key: () -> String) {
        !isDatagen || return
        val key = TransKeyCall(key)
        acceptors.forEach { it(key) }
        keys += key
    }

    fun value(t: Translatable.() -> Unit) {
        isDatagen || return
        keys.forEach(t)
        acceptors += t
    }

    override fun tran(
        data: LangData, value: () -> String
    ) { value { tran(data, value) } }
}

// endregion

fun String.tran(): TransKey = TransKey(this)

fun ItemConvertible.transKey(): TransKey = TransKey(asItem().translationKey)
fun ItemConvertible.lazyTran(): TransKeyCall = TransKeyCall { asItem().translationKey }

infix fun <I: ItemConvertible> I.tran(f: TransKey.() -> Unit)
: I = this.also { f(transKey()) }
infix fun <I: ItemConvertible> I.lazyTran(f: TransKeyCall.() -> Unit)
: I = this.also { f(lazyTran()) }
