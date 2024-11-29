package moyingji.idonknowa.core.artifact

import kotlinx.serialization.*
import moyingji.idonknowa.Idonknowa.id
import moyingji.idonknowa.MutableText
import moyingji.idonknowa.core.Regs.ARTI_AFFIX
import moyingji.idonknowa.core.Regs.ARTI_AFFIX_VALUE
import moyingji.idonknowa.core.Regs.ARTI_AFFIX_WEIGHT
import moyingji.idonknowa.core.Regs.ARTI_EFFECT
import moyingji.idonknowa.lang.TranKey
import moyingji.lib.math.randomWithWeight
import moyingji.lib.util.*

@Serializable
data class Artifact(
    @SerialName("effect") val effectId: String,
    val piece: Piece,
    val main: Affix,
    val affix: List<Affix>,

    @Transient val effect: Effect = ARTI_EFFECT[effectId.id]
        ?: throw IllegalArgumentException()
) {
    constructor(
        effect: Effect, piece: Piece,
        main: Affix, affix: List<Affix>
    ) : this(
        ARTI_EFFECT.firstKeyOf(effect).toString(),
        piece, main, affix, effect = effect)

    init {
        // EffectId.path 必须与 Effect.name 相等
        require(effectId.split(':').last() == effect.name)
    }

    @Serializable
    data class Affix (
        @SerialName("type") val typeId: String,
        val values: List<String>, // throw NumberFormatException

        @Transient val type: AffixType = ARTI_AFFIX[typeId.id]
            ?: throw IllegalArgumentException()
    ) {
        constructor(type: AffixType, values: List<String>) : this(
            ARTI_AFFIX.firstKeyOf(type).toString(),
            values, type)

        init { require(typeId.split(':').last() == type.name) }
    }

    enum class Piece (
        vararg val possibleMain: PossibleMain
    ) {
        FLOWER,
        PLUME,
        SANDS,
        GOBLET,
        CIRCLE;

        class PossibleMain (
            val id: String,
            val weight: UShort = 1u,
            vararg val values: String
        ) { init { require(weight > 0u) } }

        fun tranKey(): TranKey = PIECE_KEY_PRE.suffix(name.lowercase())
    }

    open class Effect {
        open val name: String = "none"
        fun tranKey(): TranKey = EFFECT_KEY_PRE.suffix(name.lowercase())

        fun pieceKey(piece: Piece): TranKey = NAME_KEY_PRE
            .suffix(name.lowercase())
            .suffix(piece.name.lowercase())

        open val levelAffect: List<Pair<Int, Int>> = listOf(2 to 1, 4 to 2)
    }

    open class AffixType {
        open val name: String = "none"
        fun tranKey(): TranKey = AFFIX_KEY_PRE.suffix(name.lowercase())
        fun tranValue(): TranKey = tranKey().suffix("v")
            .withArg("%s", "value") // 下以 text 传入 String 来填入参数
        fun tranValue(value: String): MutableText = tranValue().text(value)
    }

    companion object {
        val KEY_PREFIX = TranKey("artifact.idonknowa")
        val PIECE_KEY_PRE: TranKey = KEY_PREFIX.suffix("piece")
        val EFFECT_KEY_PRE: TranKey = KEY_PREFIX.suffix("effect")
        val AFFIX_KEY_PRE: TranKey = KEY_PREFIX.suffix("affix")
        val NAME_KEY_PRE: TranKey = KEY_PREFIX.suffix("name")
    }

    fun tranKey(): TranKey = effect.pieceKey(piece)

    object Ran {
        fun random(
            set: String, // EffectId
            piece: Piece? = null,
            random: (until: Int) -> Int
        ): Artifact {
            assert(set.id in ARTI_EFFECT.keys)
            // 套装 / 主词条
            val p: Piece = piece ?: Piece.entries.random(random)
            val pm = randomWithWeight(
                p.possibleMain, random) { it.weight.toInt() }
            val main = Affix(pm.id, listOf(pm.values[0]))
            // 副词条部分
            val affixes: MutableList<Affix> = mutableListOf()
            val affixTypeIds = ARTI_AFFIX.keys
                .map { it.toString() }
                .let { HashSet(it) }
            affixTypeIds -= pm.id
                // random5/4 -> 0,0,0,1; 副词条个数 | 重复随机副词条
            repeat(random(5)/4 + 3) {
                // throw NullPointerException
                val r = randomWithWeight(affixTypeIds, random) {
                    ARTI_AFFIX_WEIGHT[it.id]?.toInt() ?: 100 }!!
                val v = ARTI_AFFIX_VALUE[r.id].random(random).toString()
                affixes += Affix(r, listOf(v))
            }
            return Artifact(set, p, main, affixes)
        }
    }
}