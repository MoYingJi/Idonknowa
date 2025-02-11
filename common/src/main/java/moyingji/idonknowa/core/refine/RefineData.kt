package moyingji.idonknowa.core.refine

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import moyingji.idonknowa.core.Regs
import moyingji.idonknowa.serialization.*
import moyingji.idonknowa.util.*
import net.minecraft.network.codec.*
import net.minecraft.util.Identifier

data class RefineData (
    val id: Identifier,
    val level: Int = 1,
) : TooltipProcessor {
    companion object {
        val CODEC: Codec<RefineData>
        = RecordCodecBuilder.create { it.group(
            Identifier.CODEC.fieldOf("id").forGetter { it.id },
            Codec.INT.fieldOf("level").forGetter { it.level },
        ).apply(it, ::RefineData) }

        val PACKET_CODEC: CoPRB<RefineData>
        = PacketCodec.tuple(
            Identifier.PACKET_CODEC, { it.id },
            PacketCodecs.INTEGER, { it.level },
            ::RefineData
        )

        val CODEC_PAIR: CoTup<RefineData> = CODEC to PACKET_CODEC
    }

    val refine: Refine = Regs.REFINE[id]!!
    init { require(level in 1..refine.maxRefineLevel) }

    override fun processTooltip(tooltip: TooltipArgs) {
        refine.appendTooltip(tooltip, level)
    }
}
