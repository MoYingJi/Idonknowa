package moyingji.idonknowa.mia.mixin;

import moyingji.idonknowa.mia.impl.RecipePropertySetMixinImpl;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipePropertySet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipePropertySet.class)
abstract class RecipePropertySetMixin {
    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    final void canUse(
        final ItemStack stack,
        final CallbackInfoReturnable<Boolean> cir
    ) {
        RecipePropertySetMixinImpl.INSTANCE.canUse(
            (RecipePropertySet)(Object) this, stack, cir
        );
    }
}
