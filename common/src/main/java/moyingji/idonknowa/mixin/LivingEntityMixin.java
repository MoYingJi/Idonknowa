package moyingji.idonknowa.mixin;

import moyingji.idonknowa.mixink.LivingEntityMixinImpl;
import moyingji.lib.core.PropIn;
import moyingji.lib.core.PropValue;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
    @ModifyVariable(
        method = "hurt",
        at = @At(value = "CONSTANT", args = "floatValue=0", ordinal = 0),
        argsOnly = true
    )
    final float beforeHurt(float value, final DamageSource source) {
        final PropIn<@NotNull Float> mutableValue = new PropValue<>(value);
        LivingEntityMixinImpl.INSTANCE.beforeHurt(
            (LivingEntity) (Object) this,
            source, mutableValue
        );
        value = mutableValue.getValue();
        return value;
    }
}
