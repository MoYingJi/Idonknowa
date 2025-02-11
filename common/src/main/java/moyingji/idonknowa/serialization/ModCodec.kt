package moyingji.idonknowa.serialization

import com.mojang.serialization.Codec
import net.minecraft.network.codec.PacketCodec

typealias CoPRB<T> = PacketCodec<in RByteBuf, T>
typealias CoTup<T> = Pair<Codec<T>, CoPRB<T>>

object ModCodec {
}
