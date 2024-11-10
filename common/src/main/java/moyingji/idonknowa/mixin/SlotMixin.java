package moyingji.idonknowa.mixin;

import moyingji.idonknowa.items.SlotMayStackPlace;
import moyingji.idonknowa.mixini.SlotMenuAccessor;
import moyingji.idonknowa.util.ModContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
abstract class SlotMixin implements SlotMenuAccessor {
    @Unique public @Nullable AbstractContainerMenu idonknowa$menu;
    @Override public final @Nullable AbstractContainerMenu idonknowa$getMenu() { return idonknowa$menu; }
    @Override public final void idonknowa$setMenu(@Nullable AbstractContainerMenu menu) { idonknowa$menu = menu; }

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    final void mayPlace(final ItemStack stack, final CallbackInfoReturnable<Boolean> cir) {
        final var self = (Slot) (Object) this;
        if (self.container instanceof ModContainer container) {
            final var r = container.mayPlace(self, stack);
            if (r != null) { cir.setReturnValue(r); return; }
        }
        if (stack.getItem() instanceof SlotMayStackPlace i
            && !i.mayStackPlace(self, stack))
            cir.setReturnValue(false);
    }

    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
    final void isActive(final CallbackInfoReturnable<Boolean> cir) {
        final var self = (Slot) (Object) this;
        if (self.container instanceof ModContainer container) {
            final var r = container.isActive(self);
            if (r != null) cir.setReturnValue(r);
        }
    }
}
