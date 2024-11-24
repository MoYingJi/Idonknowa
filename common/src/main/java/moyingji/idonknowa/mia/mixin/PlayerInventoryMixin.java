package moyingji.idonknowa.mia.mixin;

import moyingji.idonknowa.mia.PlayerInvMixinImpl;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Inventory.class)
abstract class PlayerInventoryMixin {
    @Shadow @Final private List<? extends List<ItemStack>> compartments;
    @Unique private final PlayerInvMixinImpl idonknowa$impl = new PlayerInvMixinImpl(this);

    @Inject(method = "dropAll", at = @At("HEAD"))
    final void dropAllHead(final CallbackInfo ci) {
        idonknowa$impl.dropAllHead(this.compartments);
    }
    @Inject(method = "dropAll", at = @At("RETURN"))
    final void dropAllReturn(final CallbackInfo ci) {
        idonknowa$impl.dropAllReturn(this.compartments);
    }
}
