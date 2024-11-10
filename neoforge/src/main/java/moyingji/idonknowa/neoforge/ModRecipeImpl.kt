@file:Suppress("PackageDirectoryMismatch")
package moyingji.idonknowa.recipe.neoforge

import moyingji.lib.api.static
import net.minecraft.world.item.crafting.ShapedRecipePattern.*
import kotlin.math.min

object ModRecipeImpl {
    var maxWidth: Int
        get() = getMaxWidth()
        set(value) = setCraftingSize(value, maxHeight)
    var maxHeight: Int
        get() = getMaxHeight()
        set(value) = setCraftingSize(maxWidth, value)

    var patternMaxSize: Int
        @static get() = min(maxWidth, maxHeight)
        @static set(value) = setCraftingSize(value, value)
}