package moyingji.idonknowa.mia.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import moyingji.idonknowa.mia.impl.ItemStackMixinImpl;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    // getTooltip [INVOKE] Item.appendTooltip (At.Shift.BEFORE/AFTER)
    //     -> Impl -> Events.ItemTooltip.(BEFORE/AFTER)_ITEM
    // region getTooltip
    // region const string TOOLTIP_TARGET => Item.appendTooltip
    @Unique private static final String TOOLTIP_TARGET = "Lnet/minecraft/item/Item;appendTooltip(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/item/tooltip/TooltipType;)V";
    // endregion
    @Inject(
        method = "getTooltip",
        at = @At(value = "INVOKE", target = TOOLTIP_TARGET),
        cancellable = true
    )
    final void getTooltipBefore(
        final Item.TooltipContext context,
        final PlayerEntity player,
        final TooltipType type,
        final CallbackInfoReturnable<List<Text>> cir,
        final @Local List<Text> tooltip
    ) { ItemStack self = (ItemStack) (Object) this;
        ItemStackMixinImpl.INSTANCE.getTooltip(
            self, context, player, type, cir, tooltip, false
        );
    }
    @Inject(
        method = "getTooltip",
        at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = TOOLTIP_TARGET),
        cancellable = true
    )
    final void getTooltipAfter(
        final Item.TooltipContext context,
        final PlayerEntity player,
        final TooltipType type,
        final CallbackInfoReturnable<List<Text>> cir,
        final @Local List<Text> tooltip
    ) { ItemStack self = (ItemStack) (Object) this;
        ItemStackMixinImpl.INSTANCE.getTooltip(
            self, context, player, type, cir, tooltip, true
        );
    }
    // endregion
}
