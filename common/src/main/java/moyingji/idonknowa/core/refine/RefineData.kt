package moyingji.idonknowa.core.refine

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import moyingji.idonknowa.core.Regs
import moyingji.idonknowa.serialization.*
import moyingji.idonknowa.util.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.codec.*
import net.minecraft.util.Identifier
import org.jetbrains.annotations.Contract

data class RefineData (
    val id: Identifier,
    val level: UByte = 1u,
    val comp: NbtCompound = NbtCompound()
) : TooltipProcessor {
    companion object {
        val CODEC: Codec<RefineData>
        = RecordCodecBuilder.create { it.group(
            Identifier.CODEC.fieldOf("id").forGetter(RefineData::id),
            ModCodec.UBYTE_C.fieldOf("level").forGetter(RefineData::level),
            NbtCompound.CODEC.fieldOf("comp").forGetter(RefineData::comp),
        ).apply(it, ::RefineData) }

        val PACKET_CODEC: CoPRB<RefineData>
        = PacketCodec.tuple(
            Identifier.PACKET_CODEC, RefineData::id,
            ModCodec.UBYTE_S, RefineData::level,
            PacketCodecs.NBT_COMPOUND, RefineData::comp,
            ::RefineData
        )

        val CODEC_PAIR: CoTup<RefineData> = CODEC to PACKET_CODEC
    }

    val refine: Refine = Regs.REFINE[id]!!
    init { require(level in 1u..refine.maxRefineLevel.toUInt()) }

    override fun processTooltip(tooltip: TooltipArgs) {
        refine.appendTooltip(tooltip, level)
    }

    @Contract("_ -> new")
    inline infix fun comp(
        f: NbtCompound.() -> Unit
    ): RefineData = this.copy(comp = comp.copy().apply(f))

    @Contract("_ -> new")
    inline infix fun mut(
        f: NbtCompound.(Mut) -> Unit
    ): RefineData {
        val mut = Mut(id, level)
        val comp = comp.copy()
        f(comp, mut)
        return this.copy(mut.id, mut.level, comp)
    }
    data class Mut (
        var id: Identifier,
        var level: UByte
    )
}
