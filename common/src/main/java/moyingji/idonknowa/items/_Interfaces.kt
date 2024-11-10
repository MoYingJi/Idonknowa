package moyingji.idonknowa.items

import moyingji.idonknowa.*
import moyingji.idonknowa.core.Events
import moyingji.idonknowa.items.StackHasOwner.Companion.owner
import moyingji.idonknowa.lang.*
import moyingji.idonknowa.mixin.*
import moyingji.idonknowa.mixink.*
import moyingji.idonknowa.serialization.*
import moyingji.idonknowa.util.*
import moyingji.lib.api.*
import moyingji.lib.core.PropertyMap.map
import net.minecraft.ChatFormatting.GRAY
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.*
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.*
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.*
import java.util.*

fun initItemInterfacesKt() {
    StackHasOwner // init
    RestoreAfterDied // init
    BindingCurseItem // init
}

interface StackInitListener {
    /** 调用于 [ItemStackMixin.afterInit] */
    fun initedItemStack(stack: ItemStack) {}
}
interface StackCustomRarity {
    /**
     * 调用于 [ItemStackMixin.getRarity]
     * 优先级高于 [rarityComponent]
     * @return [stack] 的稀有度 为 `null` 表示默认
     */
    fun getRarity(stack: ItemStack): Rarity? = null
}
interface StackDamageCanBreak {
    /**
     * 调用于 [ItemStackMixinImpl.hurtBeforeBreak]
     * @return 是否允许损坏
     */
    fun beforeStackBreak(
        stack: ItemStack, level: ServerLevel, player: LivingEntity?
    ): Boolean = true
}
interface StackHasOwner {
    /** 调用于 [ItemStackMixin.inventoryTick] */
    @Final fun invTickOwner(stack: ItemStack, entity: Entity) {
        if (stack.has(NBT.value()) || stack.owner == null) return
        if (entity !is Player) return
        if (!allowBeOwned(stack, entity)) return
        recordOwner(stack, entity)
    }

    fun recordOwner(stack: ItemStack, player: Player) { stack.owner = player.uuid }
    fun allowBeOwned(stack: ItemStack, player: Player): Boolean = true

    companion object {
        @Name("stack_owner")
        val NBT by nbtType(ModCodecs.UUID_P)
        private val f: ItemStack.(UUID?) -> UUID? = {
            if (item is StackHasOwner) it else throw IllegalArgumentException() }
        var ItemStack.owner by NBT.property().map(f, f)

        init { Events.ItemTooltip.AFTER_ITEM.register {
            if (it.item !is StackHasOwner) return@register
            val uuid = it.stack.owner
            val name = ClientUtil.tryGetName(uuid, it.player)
            Translations.OWNER.text()
                .withStyle(GRAY)
                .append(": ")
                .append(name)
        } }
    }
}
interface SlotMayStackPlace {
    /** 调用于 [SlotMixin.mayPlace] */
    fun mayStackPlace(slot: Slot, stack: ItemStack): Boolean = true
}
interface RestoreAfterDied {
    /**
     * 调用于 [PlayerInvMixinImpl.shouldRestore]
     * @return 是否在死亡 ([Inventory.dropAll]) 后恢复物品
     */
    fun shouldRestoreAfterDied(stack: ItemStack, inv: Inventory): Boolean = true

    companion object {
        /** 调用于 [PlayerInvMixinImpl.shouldRestore] */
        @Name("restore_after_died")
        val NBT by nbtType(ModCodecs.BOOL_P)
        /** 调用于 [PlayerInvMixinImpl.shouldRestore] */
        var ItemStack.restoreAfterDied by NBT.property().default(false)
    }
}
interface ModifyStackDrop {
    /**
     * 调用于 [PlayerMixin.dropReturn]
     * 发生时 [ItemEntity] 刚被设置完成且未被生成于世界
     */
    fun modifyStackDrop(
        original: ItemEntity?, droppedItem: ItemStack, player: Player,
        dropAround: Boolean, includeThrowerName: Boolean
    ): ItemEntity? = modifyStackDrop(original, droppedItem, player)

    fun modifyStackDrop(
        original: ItemEntity?, droppedItem: ItemStack, player: Player
    ): ItemEntity? = original
}

// region interface BindingCurseItem | 绑定诅咒
/**
 * ## BindingCurseItem (I) | (强化) 绑定诅咒
 *
 * 实现此接口的物品 在 [isBindingCurse] 为 `true`(默认) 时被视为带有 **强化绑定诅咒**
 * 其会阻止玩家丢弃物品并防止其离开背包（[bindingEntity] 控制是否作用 默认排除创造模式下的玩家 部分未实现）
 * [mayRestoreAfterDied] 会控制是否在死亡 ([Inventory.dropAll]) 后恢复物品
 *
 * 作用 **强化绑定诅咒** 的物品 将会在 [Events.ItemTooltip.AFTER_ITEM] 中提示
 * 深紫色本地化文本 "绑定诅咒" 并在末尾添加用于区分原版的符号
 */
interface BindingCurseItem : SlotMayStackPlace, RestoreAfterDied, ModifyStackDrop {
    @Final override fun mayStackPlace(slot: Slot, stack: ItemStack): Boolean {
        if (!isBindingCurse(stack)) return true
        val c = slot.container
        if (c !is Inventory) return false
        if (stack.item is StackHasOwner) {
            return c.player.uuid == stack.owner }
        return true
    }
    @Final override fun modifyStackDrop(
        original: ItemEntity?, droppedItem: ItemStack, player: Player
    ): ItemEntity? {
        if (original == null) return null
        if (!bindingEntity(player)) return original
        return if (!this.isBindingCurse(droppedItem)) original else null.also {
            player.addItem(original.item.copy())
            original.item.count(0)
        }
    }
    @Final override fun shouldRestoreAfterDied(stack: ItemStack, inv: Inventory)
    : Boolean = this.isBindingCurse(stack) && this.mayRestoreAfterDied(stack)

    fun bindingEntity(entity: Entity?): Boolean = bindingCreative()
        || entity !is Player || !entity.isCreative

    fun isBindingCurse(stack: ItemStack): Boolean = true
    fun mayRestoreAfterDied(stack: ItemStack): Boolean = true
    fun bindingCreative(): Boolean = false

    companion object { init {
        Events.ItemTooltip.AFTER_ITEM.register {
            val item = it.item
            if (item !is BindingCurseItem || !item.isBindingCurse(it.stack))
                return@register
            it += "enchantment.minecraft.binding_curse".tranText()
                .withStyle(Formatting.DARK_PURPLE)
                .append(" [I]".textStyle(Formatting.GRAY))
        }
        Events.Player.STACK_INV_TICK.register {
            if (!isServerThread()) return@register
            val stack = it.stack
            val item = stack.item
            if (item !is StackHasOwner) return@register
            if (item !is BindingCurseItem) return@register
            val uuid = stack.owner ?: return@register
            val player = it.player
            if (uuid == player.uuid) return@register
            val owner = Idonknowa.currentServer
                ?.playerList?.getPlayer(uuid) ?: return@register
            if (!item.bindingEntity(owner)) return@register
            val other = stack.copy()
            stack.count(0)
            owner.addItem(other)
        }
    } }
}
// endregion