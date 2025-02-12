package moyingji.idonknowa.mia.mixin;

import moyingji.idonknowa.mia.impl.ItemMixinImpl;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
abstract class ItemMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    final void afterInit(
        final Item.Settings settings,
        final CallbackInfo ci
    ) { ItemMixinImpl.INSTANCE.afterInit(
        (Item)(Object) this, settings, ci); }
}
