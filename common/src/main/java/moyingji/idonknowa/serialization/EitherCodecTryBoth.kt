package moyingji.idonknowa.serialization

import com.mojang.datafixers.util.Either
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.*
import com.mojang.serialization.codecs.EitherCodec

data class EitherCodecTryBoth<R>(
    val left: Codec<R>,
    val right: Codec<R>,
    // 顺序优先 / 信息完整度优先
    val decodeOrder: Boolean = false,
    val encodeOrder: Boolean = true,
) : Codec<Either<R, R>> {
    override fun <T> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<Either<R, R>, T>> {
        // 读取并判断全部成功
        val lr = left.decode(ops, input)
            .map { it.mapFirst { Either.left<R, R>(it) } }
        if (lr.isSuccess || (decodeOrder && lr.hasResultOrPartial())) return lr
        val rr = right.decode(ops, input)
            .map { it.mapFirst { Either.right<R, R>(it) } }
        if (rr.isSuccess || (decodeOrder && rr.hasResultOrPartial())) return rr
        // 判断部分成功
        if (!decodeOrder && lr.hasResultOrPartial()) return lr
        if (!decodeOrder && rr.hasResultOrPartial()) return rr
        // 失败
        return DataResult.error { StringBuilder()
            .append("Failed to parse either. First: ")
            .append(lr.error().orElseThrow().message())
            .append("; Second: ")
            .append(rr.error().orElseThrow().message())
            .toString()
        }
    }

    override fun <T> encode(input: Either<R, R>, ops: DynamicOps<T>, prefix: T): DataResult<T>
    = if (encodeOrder) encodeA(input, ops, prefix) else encodeB(input, ops, prefix)

    private fun <T> encodeA(input: Either<R, R>, ops: DynamicOps<T>, prefix: T): DataResult<T> {
        val el: (R) -> DataResult<T> = { l -> left.encode(l, ops, prefix) }
        val er: (R) -> DataResult<T> = { r -> right.encode(r, ops, prefix) }
        val lr = input.map(el, er)
        if (lr.hasResultOrPartial()) return lr
        val rl = input.map(er, el)
        return if (rl.hasResultOrPartial()) rl else lr
    }
    private fun <T> encodeB(input: Either<R, R>, ops: DynamicOps<T>, prefix: T): DataResult<T> {
        var isR = false
        val v = input.left().orElseGet { input.right().orElseThrow().also { isR = true } }
        val lr = left.encode(v, ops, prefix)
        if (lr.isSuccess || (encodeOrder && !isR && lr.hasResultOrPartial())) return lr
        val rr = right.encode(v, ops, prefix)
        if (rr.isSuccess || (encodeOrder && isR && rr.hasResultOrPartial())) return rr
        return if (!isR) lr else rr
    }

    companion object {
        fun <T> codecBoth(left: Codec<T>, right: Codec<T>): EitherCodecTryBoth<T>
        = EitherCodecTryBoth(left, right).also { if (left == right) throw IllegalArgumentException() }
        fun <T> EitherCodec<T, T>.tryBoth(): EitherCodecTryBoth<T> = codecBoth(first(), second())
    }
}