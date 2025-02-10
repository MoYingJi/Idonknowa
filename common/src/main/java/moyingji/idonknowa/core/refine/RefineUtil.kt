package moyingji.idonknowa.core.refine

import moyingji.idonknowa.nbt.*
import net.minecraft.item.ItemStack

class RefineValuesBuildScope(val refine: Refine) {
    infix fun Int.values(f: Level.() -> Unit) { f(Level(this)) }

    inner class Level(val level: Int) {
        val values: Refine.MutableDataValues = refine.data
            as? Refine.MutableDataValues
            ?: throw IllegalArgumentException()

        infix fun String.to(value: String?) {
            values.setValue(level, this, value)
        }
    }
}

infix fun <R: Refine> R.build(
    f: RefineValuesBuildScope.() -> Unit
): R = this.also { f(RefineValuesBuildScope(this)) }


var ItemStack.refineData: RefineData? by ModDataComps.REFINE.prop()
