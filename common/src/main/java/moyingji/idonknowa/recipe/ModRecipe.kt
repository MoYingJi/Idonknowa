package moyingji.idonknowa.recipe

import moyingji.idonknowa.*
import moyingji.idonknowa.all.*
import moyingji.idonknowa.mia.RecipeManagerMixinImpl
import moyingji.idonknowa.recipe.builder.*
import moyingji.idonknowa.recipe.recipes.RefineRecipe
import moyingji.lib.api.static
import net.minecraft.data.recipes.*
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.*

object ModRecipe {
    val builders: MutableCollection<Pair<RecipeBuilder, Id>> = mutableListOf()
    val recipes: MutableCollection<Triple<Recipe<*>, RecipeType<*>, Id>> = mutableListOf()
    var patternMaxSize: Int
        @static @platformed get() = throw NotImplementedError()
        @static @platformed set(_) = throw NotImplementedError()

    var isRegAfterResAllowed: Boolean = false
    fun SingleRecipe.singleRecipe() {
        recipes += Triple(instance, instance.type, id)
        if (!RecipeManagerMixinImpl.isApplied) return
        val info = "A new recipe ($id) is registered but the recipe resources is loaded and be immutable"
        if (!isRegAfterResAllowed) throw IllegalStateException(info) else Idonknowa.warn(info)
    }

    init {
        IdRecipeSerializer // init
    }

    // 动态配方
    init {
        RefineRecipe.singleRecipe()
    }

    // 静态配方
    init { if (Idonknowa.isDatagen) {
        // region 原石矿 ---熔炉--> 原石
        ModRecipeBuilders.cooking()
            .result(ModItem.PRIMOGEM)
            .input(ModBlock.PRIMOGEM_ORE).build()
            .unlockedByHas(ModBlock.PRIMOGEM_ORE)
            .regSingle("from_ore")
        // endregion
        // region 摩拉 <--熔炉--> 金锭
        ModRecipeBuilders.cooking()
            .result(Items.GOLD_INGOT).input(ModItem.MORA)
            .exp(7).time(200).build()
            .unlockedByHas(ModItem.MORA)
            .regSingle("from_mora")
        ModRecipeBuilders.cooking()
            .result(ModItem.MORA).input(Items.GOLD_INGOT)
            .exp(0).time(4000).build()
            .unlockedByHas(Items.GOLD_INGOT)
            .unlockedByHas(ModItem.MORA)
            .regSingle("from_gold")
        // endregion
        // region 异界研究台 [合成]
        ModRecipeBuilders.shaped()
            .result(ModBlock.ISEKAI_RESEARCH_TABLE).build()
            .pattern("+*+")
            .pattern("&#*")
            .pattern("/-$")
            .define('#', ModBlock.PRIMOGEM_ORE)
            .define('*', ModItem.PRIMOGEM)
            .define('-', Items.NETHERITE_INGOT)
            .define('+', Items.GLASS, Items.GLASS_PANE) // 玻璃 或 玻璃板
            .define('/', Items.SPYGLASS) // 望远镜
            .define('$', Items.WAXED_OXIDIZED_COPPER_BULB) // 涂蜡的氧化铜灯
            .define('&', Items.NAUTILUS_SHELL) // 鹦鹉螺壳
            .unlockedByHasResult()
            .unlockedByHas(ModItem.PRIMOGEM)
            .regSingle()
        // endregion
    } }
}