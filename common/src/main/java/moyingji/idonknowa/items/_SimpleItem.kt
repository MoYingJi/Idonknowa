package moyingji.idonknowa.items

import moyingji.idonknowa.all.ItemSettings
import moyingji.idonknowa.core.RegHelper
import moyingji.idonknowa.datagen.withFlatModel
import moyingji.idonknowa.util.isOf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.*
import net.minecraft.world.item.*

// region CurioRemoverItem | 奇物丢弃器
class CurioRemoverItem : Item(ItemSettings()
    .stacksTo(1)
    .rarity(Rarity.UNCOMMON)
) {
    companion object {
        val regHelper = RegHelper
        .item { CurioRemoverItem() }
        .withFlatModel()
    }

    override fun overrideStackedOnOther(
        stack: ItemStack,
        slot: Slot, action: ClickAction,
        player: Player
    ): Boolean {
        if (action != ClickAction.SECONDARY) return false
        val container = slot.container
        if (container !is Inventory) return false
        if (player != container.player) return false
        val other = slot.item
        if (!other.isOf<CurioItem>()) return false
        other.shrink(1)
        return true
    }
}
// endregion