package moyingji.idonknowa.serialization

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import moyingji.idonknowa.core.refine.RefineData
import moyingji.lib.game.wish.WishData
import net.minecraft.network.codec.*

typealias CoPRB<T> = PacketCodec<in RByteBuf, T>
typealias CoTup<T> = Pair<Codec<T>, CoPRB<T>>

object ModCodec {
    val UBYTE_C: Codec<UByte> = Codec.BYTE.xmap(Byte::toUByte, UByte::toByte)
    val UBYTE_S: CoPRB<UByte> = PacketCodecs.BYTE.xmap(Byte::toUByte, UByte::toByte)
    val UBYTE_P: CoTup<UByte> = UBYTE_C to UBYTE_S

    val USHORT_C: Codec<UShort> = Codec.SHORT.xmap(Short::toUShort, UShort::toShort)
    val USHORT_S: CoPRB<UShort> = PacketCodecs.SHORT.xmap(Short::toUShort, UShort::toShort)
    val USHORT_P: CoTup<UShort> = USHORT_C to USHORT_S

    val UINT_C: Codec<UInt> = Codec.INT.xmap(Int::toUInt, UInt::toInt)
    val UINT_S: CoPRB<UInt> = PacketCodecs.INTEGER.xmap(Int::toUInt, UInt::toInt)
    val UINT_P: CoTup<UInt> = UINT_C to UINT_S


    val REFINE_DATA_C: Codec<RefineData> = RefineData.CODEC
    val REFINE_DATA_S: CoPRB<RefineData> = RefineData.PACKET_CODEC
    val REFINE_DATA_P: CoTup<RefineData> = RefineData.CODEC_PAIR


    // region WishData
    val WISH_DATA_C: Codec<WishData> = RecordCodecBuilder.create { it.group(
        UINT_C.fieldOf("leftTotal").forGetter(WishData::leftTotal),
        UINT_C.fieldOf("left").forGetter(WishData::left),
        USHORT_C.fieldOf("last4").forGetter(WishData::last4),
        USHORT_C.fieldOf("last5").forGetter(WishData::last5),
        UBYTE_C.fieldOf("lastUp4").forGetter(WishData::lastUp4),
        UBYTE_C.fieldOf("lastUp5").forGetter(WishData::lastUp5),
    ).apply(it, ::WishData) }
    val WISH_DATA_S: CoPRB<WishData> = PacketCodec.tuple(
        UINT_S, WishData::leftTotal,
        UINT_S, WishData::left,
        USHORT_S, WishData::last4,
        USHORT_S, WishData::last5,
        UBYTE_S, WishData::lastUp4,
        UBYTE_S, WishData::lastUp5,
        ::WishData
    )
    val WISH_DATA_P: CoTup<WishData> = WISH_DATA_C to WISH_DATA_S
    // endregion
}
