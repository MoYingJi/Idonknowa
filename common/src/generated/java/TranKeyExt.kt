// 代码自动生成 请勿更改
// The code is automatically generated, do not change it.
@file:Suppress("PackageDirectoryMismatch")
package moyingji.idonknowa.lang

import moyingji.idonknowa.core.RegS

infix fun net.minecraft.world.level.ItemLike.tranTo(value: String) { tranKey().tranTo(value) }
@JvmName("tranItemLikeKey") fun RegS<out net.minecraft.world.level.ItemLike>.tranKey(): TranKey = this.value().tranKey()
@JvmName("tranItemLikeTo") infix fun RegS<out net.minecraft.world.level.ItemLike>.tranTo(value: String) { tranKey().tranTo(value) }
@JvmName("lazyKeySuffixItemLike") infix fun RegS<out net.minecraft.world.level.ItemLike>.lazyKeySuffix(suffix: String): LazyTranslation = LazyTranslation().also { tran -> listen { tran.provideKey(it.tranKey()) } }
infix fun net.minecraft.world.level.ItemLike.lazyKeySuffix(suffix: String): Lazy<TranKey> = lazy { tranKey().suffix(suffix) }
infix fun net.minecraft.world.level.block.Block.tranTo(value: String) { tranKey().tranTo(value) }
@JvmName("tranBlockKey") fun RegS<out net.minecraft.world.level.block.Block>.tranKey(): TranKey = this.value().tranKey()
@JvmName("tranBlockTo") infix fun RegS<out net.minecraft.world.level.block.Block>.tranTo(value: String) { tranKey().tranTo(value) }
@JvmName("lazyKeySuffixBlock") infix fun RegS<out net.minecraft.world.level.block.Block>.lazyKeySuffix(suffix: String): LazyTranslation = LazyTranslation().also { tran -> listen { tran.provideKey(it.tranKey()) } }
infix fun net.minecraft.world.level.block.Block.lazyKeySuffix(suffix: String): Lazy<TranKey> = lazy { tranKey().suffix(suffix) }
