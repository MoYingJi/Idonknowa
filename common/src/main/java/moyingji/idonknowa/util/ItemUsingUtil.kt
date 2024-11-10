package moyingji.idonknowa.util

import net.minecraft.world.*
import net.minecraft.world.InteractionResult.*
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

typealias ItResult = InteractionResult
typealias ItResultWith<T> = InteractionResultHolder<T>
typealias ItHand = InteractionHand

class ItemUsingArgs(
    val world: Level,
    val player: Player,
    val hand: ItHand
) {
    val stack: ItemStack get() = player.getItemInHand(hand)
    val item: Item get() = stack.item

    fun startUsingItem() { player.startUsingItem(hand) }
    fun consumeUsingItem(): ItResultWith<ItemStack>
    = consume().also { startUsingItem() }

    // region InteractionResultHolder<ItemStack>
    fun success(): ItResultWith<ItemStack>
    = ItResultWith.success(stack)
    fun successNoItemUsed(): ItResultWith<ItemStack>
    = ItResultWith(SUCCESS_NO_ITEM_USED, stack)
    fun consume(): ItResultWith<ItemStack>
    = ItResultWith.consume(stack)
    fun fail(): ItResultWith<ItemStack>
    = ItResultWith.fail(stack)
    fun pass(): ItResultWith<ItemStack>
    = ItResultWith.pass(stack)
    // endregion
}

interface ItemUsingUtil {
    fun onUse(args: ItemUsingArgs): ItResultWith<ItemStack> = args.pass()
}