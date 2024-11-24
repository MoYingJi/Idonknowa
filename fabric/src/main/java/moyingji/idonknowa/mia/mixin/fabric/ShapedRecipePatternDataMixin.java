package moyingji.idonknowa.mia.mixin.fabric;

import moyingji.idonknowa.recipe.ModRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ShapedRecipePattern.Data.class)
abstract class ShapedRecipePatternDataMixin {
    @ModifyConstant(method = "method_55096", constant = @Constant(intValue = 3))
    private static int getMaxSize(int original) {
        if (original < ModRecipe.getPatternMaxSize())
            ModRecipe.setPatternMaxSize(original);
        return Math.max(original, ModRecipe.getPatternMaxSize());
    }
}