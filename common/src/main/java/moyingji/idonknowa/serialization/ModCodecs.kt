package moyingji.idonknowa.serialization

import com.mojang.serialization.Codec
import moyingji.idonknowa.Id
import net.minecraft.core.UUIDUtil
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.*
import java.util.UUID

typealias SCodec<T> = StreamCodec<in RegistryFriendlyByteBuf, T>
typealias CodecPair<T> = Pair<Codec<T>, SCodec<T>?>

object ModCodecs {
    val UBYTE: Codec<UByte> = Codec.BYTE.xmap(Byte::toUByte, UByte::toByte)
    val UBYTE_S: SCodec<UByte> = ByteBufCodecs.BYTE.map(Byte::toUByte, UByte::toByte)
    val UBYTE_P: CodecPair<UByte> = UBYTE to UBYTE_S

    val USHORT: Codec<UShort> = Codec.SHORT.xmap(Short::toUShort, UShort::toShort)
    val USHORT_S: SCodec<UShort> = ByteBufCodecs.SHORT.map(Short::toUShort, UShort::toShort)
    val USHORT_P: CodecPair<UShort> = USHORT to USHORT_S

    val UINT: Codec<UInt> = Codec.INT.xmap(Int::toUInt, UInt::toInt)
    val UINT_S: SCodec<UInt> = ByteBufCodecs.INT.map(Int::toUInt, UInt::toInt)
    val UINT_P: CodecPair<UInt> = UINT to UINT_S



    val BOOL_P: CodecPair<Boolean> = Codec.BOOL to ByteBufCodecs.BOOL
    val INT_P: CodecPair<Int> = Codec.INT to ByteBufCodecs.INT
    val ID_P: CodecPair<Id> = Id.CODEC to Id.STREAM_CODEC
    val UUID_P: CodecPair<UUID> = UUIDUtil.CODEC to UUIDUtil.STREAM_CODEC
}