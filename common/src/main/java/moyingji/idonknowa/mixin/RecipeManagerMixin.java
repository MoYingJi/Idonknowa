package moyingji.idonknowa.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.gson.JsonElement;
import moyingji.idonknowa.mixink.RecipeManagerMixinImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(RecipeManager.class)
abstract class RecipeManagerMixin {
    @Inject(
        method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"),
        locals = LocalCapture.CAPTURE_FAILSOFT
    )
    final void beforeLoading(
        final Map<ResourceLocation, JsonElement> map,
        final ResourceManager resourceManager,
        final ProfilerFiller profilerFiller,
        final CallbackInfo ci,
        final ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> builder,
        final ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> builder2
    ) { RecipeManagerMixinImpl.INSTANCE.appendCustomInstance(builder, builder2); }
}
