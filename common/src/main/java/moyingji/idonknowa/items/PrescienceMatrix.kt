package moyingji.idonknowa.items

import moyingji.idonknowa.Text
import moyingji.idonknowa.all.ItemSettings
import moyingji.idonknowa.core.*
import moyingji.idonknowa.core.Refinable.Companion.key
import moyingji.idonknowa.datagen.withFlatModel
import moyingji.idonknowa.lang.*
import moyingji.idonknowa.rs.tag.tag
import moyingji.idonknowa.serialization.*
import moyingji.idonknowa.util.*
import moyingji.lib.collections.MutableMapsMap
import moyingji.lib.core.PropIn
import net.minecraft.ChatFormatting.*
import net.minecraft.core.BlockPos
import net.minecraft.server.level.*
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.*
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.*
import net.minecraft.world.item.*
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import kotlin.math.roundToInt

class PrescienceMatrix : Item(ItemSettings()
    .durability(1145)
    .fireResistant()
), Refinable, ItemUsingUtil, StackDamageCanBreak {
    companion object {
        val regHelper = RegHelper
            .item { PrescienceMatrix() }
            .withFlatModel()
            .tag(ItemTags.DURABILITY_ENCHANTABLE)

        val MATRIX_IS_ON: NbtTypeRegS<Boolean> by nbtType(ModCodecs.BOOL_P)
        val MATRIX_LAST_DAMAGE_TICK: NbtTypeRegS<UByte> by nbtType(ModCodecs.UBYTE_P)

        private var ItemStack.isMatrixOn: Boolean by MATRIX_IS_ON.property(false)
        private var ItemStack.lastDamageTick: UByte by MATRIX_LAST_DAMAGE_TICK.property(0u)
    }

    override val refineData: MutableMapsMap<String, UByte, String> = MutableMapsMap()
    init { refineData
        .key("maxDamage") {
            it += 1 to 9000
            it += 2 to 10000
            it += 3 to 13000
            it += 4 to 14000
            it += 5 to 15000
        }
        .key("distributed") {
            it += 1 to 60
            it += 2 to 70
            it += 4 to 80
        }
        .key("damageTick") {
            it += 1 to 50u
            it += 2 to 60u
            it += 3 to 70u
        }
    }

    init { Events.Entity.LIVING_HURT.register {
        _, amount ->
        checkInv(this)?.let { tigger(it, this, amount) }
    }; addToRefinableSet() }

    fun checkInv(living: LivingEntity): ItemStack? {
        val check = mutableSetOf(living.allSlots)
        if (living is Player) check += living.inventory.items
        for (inv in check)
            for (stack in inv)
                if (checkStack(stack)) return stack
        return null
    }
    fun checkStack(stack: ItemStack): Boolean {
        if (!stack.isOf(this)) return false
        if (stack.durability <= 0) stack.isMatrixOn = false
        return stack.isMatrixOn
    }
    fun tigger(
        stack: ItemStack,
        living: LivingEntity,
        amount: PropIn<Float>
    ) { if (!checkStack(stack)) return
        val distributed = stack.refineData("distributed")?.toInt() ?: return
        val md = amount.value * distributed/100
        val dd = (md * 120).roundToInt()
        stack.hurtAndBreak(dd, living)
        amount.value -= md
        checkStack(stack)
    }

    override fun inventoryTick(
        stack: ItemStack,
        level: Level, entity: Entity,
        i: Int, bl: Boolean
    ) { super.inventoryTick(stack, level, entity, i, bl)
        if (!checkStack(stack)) return
        stack.lastDamageTick ++
        if (stack.lastDamageTick >= (stack.refineData("damageTick")
            ?.toUByte() ?: 20u)) {
            stack.lastDamageTick = 0u
            if (isServerThread() && level is ServerLevel)
                stack.hurtAndBreak(1, level, entity as? ServerPlayer) {}
        }
    }

    override fun allowComponentsUpdateAnimation(
        player: Player,
        hand: ItHand,
        oldStack: ItemStack,
        newStack: ItemStack,
    ): Boolean = false

    override fun reloadRefineLevel(stack: ItemStack)
    { stack.refineData("maxDamage")?.let { stack.maxDamageComponent = it.toInt() } }
    override fun onUse(args: ItemUsingArgs): ItResultWith<ItemStack> {
        if (!args.stack.isMatrixOn) {
            if (checkInv(args.player) != null) return args.pass()
            args.stack.isMatrixOn = true
        } else args.stack.isMatrixOn = false
        return args.success()
    }
    override fun overrideOtherStackedOnMe(
        stack: ItemStack,
        other: ItemStack,
        slot: Slot,
        action: ClickAction,
        player: Player,
        access: SlotAccess,
    ): Boolean {
        if (!other.isEmpty) return false
        if (action != ClickAction.SECONDARY) return false
        if (!stack.isMatrixOn) {
            if (checkInv(player) != null) return false
            stack.isMatrixOn = true
        } else stack.isMatrixOn = false
        return true
    }

    override fun isFoil(stack: ItemStack): Boolean = stack.isMatrixOn
    override fun canAttackBlock(s: BlockState, w: Level, p: BlockPos, l: Player): Boolean = false
    override fun getEnchantmentValue(): Int = 1
    override fun beforeStackBreak(stack: ItemStack, level: ServerLevel, player: LivingEntity?): Boolean = false
    override fun appendTooltip(tooltip: TooltipArgs) {
        super.appendTooltip(tooltip)
        val stack = tooltip.stack
        // state
        Text.empty()
            .append(Translations.CURRENT_STATE.text().withStyle(DARK_GRAY))
            .append(": ".textStyle(DARK_GRAY))
            .append(if (stack.isMatrixOn) Translations.STATE_ON.text().withStyle(GRAY)
            else Translations.STATE_OFF.text().withStyle(DARK_GRAY))
            .also { tooltip += it }
        // durability
        tooltip.appendDurability(DARK_GRAY, GRAY)
    }
    override fun appendAdvancedDurability(stack: ItemStack): Boolean = false
}