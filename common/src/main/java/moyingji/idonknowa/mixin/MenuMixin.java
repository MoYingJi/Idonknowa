package moyingji.idonknowa.mixin;

import moyingji.idonknowa.mixini.SlotMenuAccessor;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerMenu.class)
abstract class MenuMixin {
    @Inject(method = "addSlot", at = @At(value = "RETURN"))
    final void addSlot(Slot slot, CallbackInfoReturnable<Slot> cir) {
        final var self = (AbstractContainerMenu) (Object) this;
        if (slot instanceof SlotMenuAccessor accessor)
            accessor.idonknowa$setMenu(self);
    }
}
