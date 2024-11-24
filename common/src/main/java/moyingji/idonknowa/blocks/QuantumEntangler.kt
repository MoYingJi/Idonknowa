package moyingji.idonknowa.blocks

import moyingji.idonknowa.Text
import moyingji.idonknowa.all.BlockSettings
import moyingji.idonknowa.core.*
import moyingji.idonknowa.datagen.*
import moyingji.idonknowa.gui.*
import moyingji.idonknowa.lang.text
import moyingji.idonknowa.rs.loot.dropSelf
import moyingji.idonknowa.rs.tag.tag
import moyingji.idonknowa.util.ItResult
import net.fabricmc.api.EnvType.CLIENT
import net.fabricmc.api.Environment
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.tags.BlockTags
import net.minecraft.world.*
import net.minecraft.world.entity.player.*
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.phys.BlockHitResult

class QuantumEntangler : Block(BlockSettings.of()
    .mapColor(MapColor.METAL)
    .requiresCorrectToolForDrops()
    .strength(7F, 1200F)
), MenuProvider {
    companion object {
        val regHelper: BlockRegHelper = RegHelper
            .block { QuantumEntangler() }
            .withSimpleModel()
            .dropSelf()
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .tag(BlockTags.NEEDS_IRON_TOOL)
            .blockItem()

        val menuType by regMenu(::Menu) { ::Screen }
    }

    override fun createMenu(i: Int, inventory: Inventory, player: Player): Menu = Menu(i, inventory)
    override fun getDisplayName(): Text = this.name
    override fun useWithoutItem(
        blockState: BlockState,
        level: Level,
        blockPos: BlockPos,
        player: Player,
        blockHitResult: BlockHitResult,
    ): ItResult {
        player.displayClientMessage("Quantum Entangler".text(), true)
        player.openExtendedMenu(this)
        return ItResult.SUCCESS_NO_ITEM_USED
    }

    class Menu(syncId: Int, inv: Inventory) : AbstractContainerMenu(menuType.value(), syncId) {
        override fun stillValid(player: Player): Boolean = true

        val container = SimpleContainer(64)
        val config = SlotXY(containerRows = 6)
        init {
            addPlayerInvSlot(inv, ::addSlot, config)
            addContainerSlot(container, ::addSlot, config)
        }
        override fun quickMoveStack(player: Player, i: Int): ItemStack {
            return ItemStack.EMPTY
        }
    }
    @Environment(CLIENT)
    class Screen(menu: Menu, playerInv: Inventory, title: Component) : HandledScreen<Menu>(menu, playerInv, title) {
        override fun render(guiGraphics: GuiGraphics, i: Int, j: Int, f: Float) {
            super.render(guiGraphics, i, j, f)
            renderTooltip(guiGraphics, i, j)
        }
        init {
            val rows = menu.config.containerRows
            imageHeight = 114 + rows * 18
            inventoryLabelY = imageHeight - 94
        }
        override fun renderBg(guiGraphics: GuiGraphics, f: Float, i: Int, j: Int) {
        }
    }
}