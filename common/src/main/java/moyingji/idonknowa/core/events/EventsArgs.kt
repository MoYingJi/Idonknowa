package moyingji.idonknowa.core.events

import moyingji.lib.api.Mutable
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

data class PlayerDropItemModifier(
    @Mutable var original: ItemEntity?,
    val droppedItem: ItemStack,
    val player: Player,
    val dropAround: Boolean,
    val includeThrowerName: Boolean
)

data class InventoryTickArgs(
    val stack: ItemStack,
    val level: Level,
    val entity: Entity,
    val inventorySlot: Int, // slotId
    val isCurrentItem: Boolean // isSelected
) {
    val player: Player get() = entity as Player
}