package moyingji.idonknowa.mia.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import moyingji.idonknowa.mia.impl.ServerRecipeManagerMixinImpl;
import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.SortedMap;

@Mixin(ServerRecipeManager.class)
abstract class ServerRecipeManagerMixin {
    @Inject(
        method = "prepare(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)Lnet/minecraft/recipe/PreparedRecipes;",
        at = @At(value = "INVOKE", target = "Ljava/util/SortedMap;forEach(Ljava/util/function/BiConsumer;)V")
    )
    final void beforeLoading(
        final ResourceManager resourceManager,
        final Profiler profiler,
        final CallbackInfoReturnable<PreparedRecipes> cir,
        final @Local SortedMap<Identifier, Recipe<?>> map
    ) { ServerRecipeManagerMixinImpl.INSTANCE.beforeLoading(
        (ServerRecipeManager)(Object) this,
        resourceManager, profiler, cir, map
    ); }
}
