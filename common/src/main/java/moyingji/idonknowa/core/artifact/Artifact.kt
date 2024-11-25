package moyingji.idonknowa.core.artifact

import kotlinx.serialization.*
import moyingji.idonknowa.Id
import moyingji.idonknowa.Idonknowa.id
import moyingji.idonknowa.core.Regs
import moyingji.idonknowa.lang.TranKey
import moyingji.lib.util.firstKeyOf

@Serializable
data class Artifact(
    @SerialName("effect") val effectId: String,
    val piece: Piece,
    val main: Affix,
    val affix: List<Affix>,

    @Transient val effect: Effect = Regs
        .ARTIFACT_EFFECT[effectId.id(Id.DEFAULT_NAMESPACE)]
        ?: throw IllegalArgumentException()
) {
    constructor(
        effect: Effect, piece: Piece,
        main: Affix, affix: List<Affix>
    ) : this(Regs.ARTIFACT_EFFECT.firstKeyOf(effect).toString(),
        piece, main, affix, effect = effect)

    init {
        // EffectId.path 必须与 Effect.name 相等
        require(effectId.split(':').last() == effect.name)
    }

    @Serializable
    data class Affix (
        @SerialName("type") val typeId: String,
        val values: List<String>, // throw NumberFormatException

        @Transient val type: AffixType = Regs
            .ARTIFACT_AFFIX[typeId.id(Id.DEFAULT_NAMESPACE)]
            ?: throw IllegalArgumentException()
    ) {
        constructor(type: AffixType, values: List<String>) : this(
            Regs.ARTIFACT_AFFIX.firstKeyOf(type).toString(),
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

        fun tranKey(): TranKey = PIECE_KEY_PREFIX.suffix(name.lowercase())
    }

    open class Effect {
        open val name: String = "none"
        fun tranKey(): TranKey = EFFECT_KEY_PREFIX.suffix(name.lowercase())

        fun pieceKey(piece: Piece): TranKey = NAME_KEY_PREFIX
            .suffix(name.lowercase())
            .suffix(piece.name.lowercase())

        open val levelAffect: List<Pair<Int, Int>> = listOf(2 to 1, 4 to 2)
    }

    open class AffixType {
        open val name: String = "none"
        fun tranKey(): TranKey = AFFIX_KEY_PREFIX.suffix(name.lowercase())
    }

    companion object {
        val KEY_PREFIX = TranKey("artifact.idonknowa")
        val PIECE_KEY_PREFIX: TranKey = KEY_PREFIX.suffix("piece")
        val EFFECT_KEY_PREFIX: TranKey = KEY_PREFIX.suffix("effect")
        val AFFIX_KEY_PREFIX: TranKey = KEY_PREFIX.suffix("affix")
        val NAME_KEY_PREFIX: TranKey = KEY_PREFIX.suffix("name")
    }

    fun tranKey(): TranKey = effect.pieceKey(piece)
}