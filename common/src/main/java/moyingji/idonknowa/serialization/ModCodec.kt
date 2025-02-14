package moyingji.idonknowa.serialization

import com.mojang.serialization.Codec
import net.minecraft.network.codec.*

typealias CoPRB<T> = PacketCodec<in RByteBuf, T>
typealias CoTup<T> = Pair<Codec<T>, CoPRB<T>>

object ModCodec {
    val UBYTE_C: Codec<UByte> = Codec.BYTE.xmap(Byte::toUByte, UByte::toByte)
    val UBYTE_S: CoPRB<UByte> = PacketCodecs.BYTE.xmap(Byte::toUByte, UByte::toByte)
    val UBYTE_P: CoTup<UByte> = UBYTE_C to UBYTE_S
}
