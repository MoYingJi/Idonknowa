@file:Suppress("PackageDirectoryMismatch")
package moyingji.idonknowa.recipe.fabric

import moyingji.lib.api.static

object ModRecipeImpl {
    var patternMaxSize: Int = 3 @static get
        @static set(value) { if (field < value) field = value }
}