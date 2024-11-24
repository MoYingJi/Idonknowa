package moyingji.idonknowa.mia.mixin.neoforge;

import moyingji.idonknowa.mia.ItemStackMixinImpl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    @Inject(
        method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"),
        cancellable = true
    )
    final void hurtBeforeBreak(
        final int damage,
        final ServerLevel level,
        final @Nullable LivingEntity living,
        final Consumer<Item> onBreak,
        final CallbackInfo ci
    ) { ItemStackMixinImpl.INSTANCE.hurtBeforeBreak(
        (ItemStack) (Object) this,
        damage, level, living, onBreak, ci
    ); }
}
