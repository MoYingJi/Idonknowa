package moyingji.idonknowa.autoreg

import moyingji.idonknowa.lang.*
import net.minecraft.block.*
import net.minecraft.item.Item
import net.minecraft.registry.RegistryKeys

typealias ItemSettings = Item.Settings
typealias BlockSettings = AbstractBlock.Settings

infix fun <S: RSProvider<T>, T> S.listen(f: (T) -> Unit): S
= this.apply { action { it.listen(f) } }

class RSItemProvider(
    settings: ItemSettings,
    factory: (ItemSettings) -> Item,
) : RSProvider.SetKey<ItemSettings, Item>(
    RegistryKeys.ITEM, settings, factory,
    ItemSettings::registryKey
) {
    infix fun tran(f: Translatable.() -> Unit): RSItemProvider
    = listen { it tran f }
}

fun item(
    factory: (ItemSettings) -> Item = ::Item,
    settings: (ItemSettings) -> Unit = {},
): RSItemProvider = RSItemProvider(ItemSettings().apply(settings), factory)

class RSBlockProvider(
    settings: BlockSettings,
    factory: (BlockSettings) -> Block,
) : RSProvider.SetKey<BlockSettings, Block>(
    RegistryKeys.BLOCK, settings, factory,
    BlockSettings::registryKey
) {
    infix fun tran(f: Translatable.() -> Unit): RSBlockProvider
    = listen { it tran f}
}

fun block(
    factory: (BlockSettings) -> Block = ::Block,
    settings: (BlockSettings) -> Unit = {},
): RSBlockProvider = RSBlockProvider(BlockSettings.create().apply(settings), factory)
