package moyingji.idonknowa.mia.impl

import moyingji.idonknowa.autoreg.ItemSettings
import moyingji.idonknowa.util.SettingsUtil
import net.minecraft.item.Item
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object ItemMixinImpl {
    fun afterInit(
        item: Item, settings: ItemSettings, ci: CallbackInfo
    ) {
        SettingsUtil.itemListener.get(settings).forEach { it.invoke(item) }
    }
}