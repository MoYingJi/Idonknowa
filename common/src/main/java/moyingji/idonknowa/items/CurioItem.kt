package moyingji.idonknowa.items

import dev.architectury.event.EventResult
import dev.architectury.event.events.common.BlockEvent
import dev.architectury.utils.value.IntValue
import moyingji.idonknowa.all.*
import moyingji.idonknowa.core.RegHelper
import moyingji.idonknowa.datagen.withFlatModel
import moyingji.idonknowa.serialization.*
import moyingji.idonknowa.util.*
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.*
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import org.jetbrains.annotations.MustBeInvokedByOverriders
import kotlin.math.min

abstract class CurioItem(properties: Properties) : Item(properties
    .stacksTo(1)
), BindingCurseItem, TooltipUtil {
    companion object {
        val CURIO_VALUE: NbtTypeRegS<UShort> by nbtType(ModCodecs.USHORT_P)
        /** `null` -> no value; `0u` -> break; else -> value */
        var ItemStack.curioValue: UShort? by CURIO_VALUE.property()
        val ItemStack.curioHasValue: Boolean get() = has(CURIO_VALUE.value()) || curioValue != null
        val ItemStack.curioIsBreak: Boolean get() = curioValue?.let { it <= 0u } == true

        val curioList: MutableList<CurioItem> = mutableListOf()
    }
    init { curioList += this }

    open fun onGetCurio(stack: ItemStack, player: Player) {}

    override fun canAttackBlock(
        state: BlockState, level: Level, pos: BlockPos, player: Player
    ): Boolean = false
}

abstract class BigLottoCurio<E>(properties: Properties) : CurioItem(properties
    .component(CURIO_VALUE.value(), 1u)
) {
    open fun tiggerLotto(stack: ItemStack, player: ServerPlayer, event: E) {}
    @MustBeInvokedByOverriders
    open fun damageLotto(stack: ItemStack, player: ServerPlayer, event: E) {
        if (!stack.curioHasValue) throw IllegalStateException()
        if (stack.curioIsBreak) throw IllegalStateException()
        stack.curioValue = 0u
    }
}






class CosmicBigLotto : BigLottoCurio<CosmicBigLotto.EventData>(ItemSettings()) {
    data class EventData(
        val level: Level,
        val pos: BlockPos,
        val state: BlockState,
        val xp: IntValue?
    )
    companion object {
        val regHelper = RegHelper
            .item { CosmicBigLotto() }
            .withFlatModel()
        init { BlockEvent.BREAK.register {
                l, bp, s, p, x ->
            p.inventory.firstOrNull {
                it.isOf(ModItem.COSMIC_BIG_LOTTO) && !it.curioIsBreak
            }?.let {
                val i = it.item as? CosmicBigLotto
                    ?: return@let
                val d = EventData(l, bp, s, x)
                i.tiggerLotto(it, p, d) }
            return@register EventResult.pass()
        } }
    }
    override fun tiggerLotto(stack: ItemStack, player: ServerPlayer, event: EventData) {
        super.tiggerLotto(stack, player, event)
        val r = event.level.random.nextInt(10)
        if (r == 0) return damageLotto(stack, player, event)
        if (r <= 3) {
            val rc = event.level.random.nextInt(curioList.size)
            val item = curioList[rc]
            val cs = item.default
            player.addItem(cs)
            item.onGetCurio(cs, player)
        }
    }
    override fun damageLotto(stack: ItemStack, player: ServerPlayer, event: EventData) {
        super.damageLotto(stack, player, event)
        player.health = min(player.health, 1F)
    }
}