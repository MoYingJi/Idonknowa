package moyingji.idonknowa.util

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import moyingji.idonknowa.autoreg.ItemSettings
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.Item

fun ItemSettings.glint(overrider: Boolean = true): ItemSettings
= component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, overrider)

fun ItemSettings.listen(listener: (Item) -> Unit): ItemSettings
= this.also { SettingsUtil.itemListener.put(this, listener) }

object SettingsUtil {
    val itemListener
    : Multimap<ItemSettings, (Item) -> Unit>
    = HashMultimap.create()
}
