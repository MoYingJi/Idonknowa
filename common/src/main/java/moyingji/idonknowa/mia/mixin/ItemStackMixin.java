package moyingji.idonknowa.mia.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import moyingji.idonknowa.core.Events;
import moyingji.idonknowa.core.events.InventoryTickArgs;
import moyingji.idonknowa.items.StackCustomRarity;
import moyingji.idonknowa.items.StackHasOwner;
import moyingji.idonknowa.items.StackInitListener;
import moyingji.idonknowa.util.ItemUsingArgs;
import moyingji.idonknowa.util.ItemUsingUtil;
import moyingji.idonknowa.util.MixinTooltipArgs;
import moyingji.idonknowa.util.TooltipUtil;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    // getTooltipLines INVOKE(Shift.NONE/AFTER) Item.appendHoverText -> Events.ItemTooltip.(BEFORE/AFTER)_ITEM
    // region (before/after)ItemTooltip
    @Inject(
        method = "getTooltipLines",
        locals = LocalCapture.CAPTURE_FAILSOFT,
        cancellable = true,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V")
    )
    final void beforeItemTooltip(
        final Item.TooltipContext tooltipContext,
        final @Nullable Player player,
        final TooltipFlag flag,
        final CallbackInfoReturnable<List<Component>> cir,
        final List<Component> list
    ) {
        final var args = new MixinTooltipArgs(this, tooltipContext, player, flag, cir, list);
        Events.ItemTooltip.INSTANCE.getBEFORE_ITEM().invoker().invoke(args);
    }
    @Inject(
        method = "getTooltipLines",
        locals = LocalCapture.CAPTURE_FAILSOFT,
        cancellable = true,
        at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/world/item/Item;appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V")
    )
    final void afterItemTooltip(
        final Item.TooltipContext tooltipContext,
        final @Nullable Player player,
        final TooltipFlag tooltipFlag,
        final CallbackInfoReturnable<List<Component>> cir,
        final List<Component> list
    ) {
        final var args = new MixinTooltipArgs(this, tooltipContext, player, tooltipFlag, cir, list);
        Events.ItemTooltip.INSTANCE.getAFTER_ITEM().invoker().invoke(args);
    }
    // endregion

    // getTooltipLines @ModifyExpressionValue isDamaged -> item&TooltipUtil.appendAdvancedDurability
    // region appendAdvancedDurability
    @ModifyExpressionValue(
        method = "getTooltipLines",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isDamaged()Z")
    )
    final boolean appendAdvancedDurability(final boolean original) {
        final var self = (ItemStack) (Object) this;
        if (self.getItem() instanceof TooltipUtil item) {
            final var ret = item.appendAdvancedDurability(self);
            if (ret != null) return ret; }
        return original;
    }
    // endregion


    // region beforeUse use HEAD
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    final void beforeUse(
        final Level level,
        final Player player,
        final InteractionHand interactionHand,
        final CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir
    ) {
        if (((ItemStack)(Object)this).getItem() instanceof ItemUsingUtil item) {
            final var result = item.onUse(new ItemUsingArgs(level, player, interactionHand));
            if (result.getResult() != InteractionResult.PASS) cir.setReturnValue(result);
        }
    }
    // endregion

    // region afterInit <init> RETURN
    @Inject(
        method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V",
        at = @At("RETURN")
    )
    final void afterInit(
        final ItemLike item,
        final int i,
        final PatchedDataComponentMap dataMap,
        final CallbackInfo ci
    ) {
        final var self = (ItemStack) (Object) this;
        if (item instanceof StackInitListener l) l.initedItemStack(self);
        else if (item.asItem() instanceof StackInitListener l) l.initedItemStack(self);
    }
    // endregion

    // region getRarity getRarity HEAD
    @Inject(method = "getRarity", at = @At("HEAD"), cancellable = true)
    final void getRarity(final CallbackInfoReturnable<Rarity> cir) {
        final var self = (ItemStack) (Object) this;
        if (self.getItem() instanceof StackCustomRarity item) {
            final var r = item.getRarity(self);
            if (r != null) cir.setReturnValue(r);
        }
    }
    // endregion

    // region inventoryTick getRarity RETURN -> Events.Player.STACK_INV_TICK
    @Inject(method = "inventoryTick", at = @At("RETURN"))
    final void inventoryTick(
        final Level level, final Entity entity,
        final int inventorySlot, final boolean isCurrentItem,
        final CallbackInfo ci
    ) {
        final var self = (ItemStack) (Object) this;
        if (self.getItem() instanceof StackHasOwner i)
            i.invTickOwner(self, entity);
        final var arg = new InventoryTickArgs(
            self, level, entity, inventorySlot, isCurrentItem);
        Events.Player.INSTANCE.getSTACK_INV_TICK().invoker().invoke(arg);
    }
    // endregion


    // Expect Platform Mixin
    // hurtBeforeBreak hurtAndBreak INVOKE shrink() -> Impl -> item&StackBeforeBreakListener.hurtBeforeBreak
}
