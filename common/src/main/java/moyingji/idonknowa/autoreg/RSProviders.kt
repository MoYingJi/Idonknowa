package moyingji.idonknowa.autoreg

import moyingji.idonknowa.lang.*
import net.minecraft.item.Item
import net.minecraft.registry.*
import net.minecraft.util.Identifier

infix fun <S: RSProvider<T>, T> S.listen(f: (T) -> Unit): S
= this.apply { action { it.listen(f) } }

class RSItemProvider(
    val settings: Item.Settings,
    val factory: (Item.Settings) -> Item,
) : RSProvider<Item>(RegistryKeys.ITEM) {
    override fun provide(id: Identifier): Item {
        val key: RegistryKey<Item> = RegistryKey.of(registry, id)
        settings.registryKey(key)
        return factory(settings)
    }

    infix fun tran(f: Translatable.() -> Unit): RSItemProvider
    = listen { it tran f }
}

fun item(
    factory: (Item.Settings) -> Item = ::Item,
    settings: (Item.Settings) -> Unit = {},
): RSItemProvider = RSItemProvider(Item.Settings().apply(settings), factory)