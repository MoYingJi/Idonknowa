package moyingji.idonknowa.items

import moyingji.idonknowa.*
import moyingji.idonknowa.Idonknowa.autoString
import moyingji.idonknowa.Idonknowa.id
import moyingji.idonknowa.all.ItemSettings
import moyingji.idonknowa.core.*
import moyingji.idonknowa.datagen.withOnlyParent
import moyingji.idonknowa.lang.*
import moyingji.lib.inspiration.MutableMapsMap
import net.minecraft.ChatFormatting.*
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.ai.attributes.*
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.*
import net.minecraft.world.item.*
import net.minecraft.world.item.Rarity.EPIC
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.level.block.state.BlockState
import kotlin.collections.set

@Suppress("ClassName")
class _TestItem : Item(ItemSettings()
    .stacksTo(1)
    .rarity(EPIC)
    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
    .attributes(ItemAttributeModifiers.builder()
        .add(Attributes.ATTACK_DAMAGE, AttributeModifier(
            BASE_ATTACK_DAMAGE_ID, 114513.0, AttributeModifier.Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND)
        .build())
), Refinable, BindingCurseItem {
    companion object { val regHelper = RegHelper
        .item { _TestItem() }
        .withOnlyParent(Items.DEBUG_STICK)
    }
    override val refineData: MutableMapsMap<String, UByte, String> = MutableMapsMap()
    init { for (i in 1u..refineMaxLevel.toUInt())
        refineData["level"][i.toUByte()] = i.toString() }
    init { addToRefinableSet() }

    override fun isCorrectToolForDrops(
        itemStack: ItemStack, blockState: BlockState
    ): Boolean = true

    override fun overrideStackedOnOther(
        stack: ItemStack,
        slot: Slot,
        click: ClickAction,
        player: Player,
    ): Boolean {
        if (click != ClickAction.SECONDARY) return false
        val other = slot.item
        if (other.isEmpty) return false
        sendNbtMsg(player, other)
        return true
    }
    fun sendNbtMsg(player: Player, stack: ItemStack) {
        player.sendSystemMessage(Text.literal("=== Idonknowa Test ==="))
        val title: MutableText = Text.empty()
        title.append(stack.displayName).append(" [Components]:")
        player.sendSystemMessage(title)
        for (c in stack.components)
            player.sendSystemMessage(Text.empty()
                .append(c.type.toString().id.autoString.textStyle(GRAY))
                .append(" = ".textStyle(DARK_GRAY))
                .append(c.value.toString())
            )
    }
}