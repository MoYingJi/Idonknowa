package moyingji.idonknowa.autoreg

import moyingji.idonknowa.lang.*
import net.minecraft.block.*
import net.minecraft.item.*
import net.minecraft.registry.RegistryKeys

typealias ItemSettings = Item.Settings
typealias BlockSettings = AbstractBlock.Settings

infix fun <S: RSProvider<T>, T> S.listen(f: RegS<T>.(T) -> Unit): S
= this.apply { action { s -> s.listen { f(s, it) } } }

inline infix fun <S: RSProvider<*>> S.apply(f: S.() -> Unit)
: S { f(); return this }

// region Item
class RSItemProvider(
    settings: ItemSettings,
    factory: (ItemSettings) -> Item,
) : RSProvider.SetKey<ItemSettings, Item>(
    RegistryKeys.ITEM, settings, factory,
    ItemSettings::registryKey
)

fun item(
    factory: (ItemSettings) -> Item = ::Item,
    settings: ItemSettings.() -> Unit = {},
): RSItemProvider = RSItemProvider(ItemSettings().apply(settings), factory)

@JvmName("tranItem")
infix fun <P: RSProvider<Item>> P.tran(
    f: Translatable.() -> Unit
): P = listen { it tran f }

// endregion

// region Block
class RSBlockProvider(
    settings: BlockSettings,
    factory: (BlockSettings) -> Block,
) : RSProvider.SetKey<BlockSettings, Block>(
    RegistryKeys.BLOCK, settings, factory,
    BlockSettings::registryKey
)

fun block(
    factory: (BlockSettings) -> Block = ::Block,
    settings: BlockSettings.() -> Unit = {},
): RSBlockProvider = RSBlockProvider(BlockSettings.create().apply(settings), factory)

@JvmName("tranBlock")
infix fun <P: RSProvider<Block>> P.tran(
    f: Translatable.() -> Unit
): P = listen { it tran f }

class BlockItemProviderData<B: Block>(
    val caller: RSProvider<B>,
    var factory: (B, ItemSettings) -> BlockItem = ::BlockItem,
    var settings: ItemSettings.() -> Unit = {},
    var setter: RSItemProvider.() -> Unit = {},
) {
    infix fun factory(factory: (B, ItemSettings) -> BlockItem)
    : BlockItemProviderData<B> = apply { this.factory = factory }
    infix fun settings(settings: ItemSettings.() -> Unit)
    : BlockItemProviderData<B> = apply { this.settings = settings }
    infix fun setter(setter: RSItemProvider.() -> Unit)
    : BlockItemProviderData<B> = apply { this.setter = setter }
}

infix fun <P: RSProvider<B>, B: Block> P.withBlockItem(
    data: BlockItemProviderData<B>.() -> Unit
): P = listen { block ->
    val dp = BlockItemProviderData<B>(this@withBlockItem)
    dp.apply(data)
    val settings = ItemSettings().apply(dp.settings)
    val provider = RSItemProvider(settings) { dp.factory(block, it) }
    provider.apply(dp.setter).register(this.id)
}

// endregion Block
