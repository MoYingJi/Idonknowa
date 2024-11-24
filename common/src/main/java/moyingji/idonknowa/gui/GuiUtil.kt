package moyingji.idonknowa.gui

import dev.architectury.registry.menu.MenuRegistry
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.*
import net.minecraft.world.entity.player.*
import net.minecraft.world.inventory.*

typealias Menu = AbstractContainerMenu
typealias HandledScreen<T> = AbstractContainerScreen<T>

fun Player.openExtendedMenu(
    provider: MenuProvider,
    bufWriter: (FriendlyByteBuf) -> Unit = {}
) { if (this !is ServerPlayer) return
    MenuRegistry.openExtendedMenu(this, provider, bufWriter) }


// region addSlot
// 此处 GUI 常数 取自 [ChestMenu]
class SlotXY(
    val containerRows: Int = 3,
    val slotSize: Int = 18,
    val startX: Int = 8,
    val containerStartY: Int = 18,
    val playerStartY: Int = 31 + containerRows * slotSize,
    val quickBarY: Int = 89 + containerRows * slotSize,
) {
    fun getX(x: Int): Int = startX + x * slotSize
    fun getContainerY(y: Int): Int = containerStartY + y * slotSize
    fun getPlayerY(y: Int): Int = playerStartY + y * slotSize
}

// 此处 GUI 常数 取自 Fabric 教程
fun addPlayerInvSlot(
    inv: Inventory,
    addSlot: (Slot) -> Unit,
    slotSize: Int = 18,
    invStartX: Int = 8,
    invStartY: Int = 84,
    quickBarY: Int = 142,
) {
    // 玩家物品栏
    for (m in 0 until 3) for (l in 0 until 9)
        addSlot(Slot(inv, l+m*9+9, invStartX+l*slotSize, invStartY+m*slotSize))
    // 玩家快捷栏
    for (m in 0 until 9)
        addSlot(Slot(inv, m, invStartX+m*slotSize, quickBarY))
}

fun addPlayerInvSlot(
    inv: Inventory,
    addSlot: (Slot) -> Unit,
    config: SlotXY
) { addPlayerInvSlot(
    inv, addSlot,
    config.slotSize, config.startX,
    config.playerStartY, config.quickBarY
) }

fun addContainerSlot(
    container: Container,
    addSlot: (Slot) -> Unit,
    config: SlotXY
) {
    for (l in 0 until config.containerRows) for (m in 0 until 9)
        addSlot(Slot(container, m+l*9, config.getX(m), config.getContainerY(l)))
}
// endregion


fun getChestMenuType(rows: Int): MenuType<ChestMenu> = when (rows) {
    1 -> MenuType.GENERIC_9x1
    2 -> MenuType.GENERIC_9x2
    3 -> MenuType.GENERIC_9x3
    4 -> MenuType.GENERIC_9x4
    5 -> MenuType.GENERIC_9x5
    6 -> MenuType.GENERIC_9x6
    else -> throw IllegalArgumentException()
}