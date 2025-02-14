package moyingji.idonknowa.core.refine

import moyingji.idonknowa.autoreg.ItemSettings
import moyingji.idonknowa.core.refine.Refine.DataValues
import moyingji.idonknowa.nbt.*
import net.minecraft.item.ItemStack

class RefineValuesBuildScope(val data: DataValues) {
    init { require(data is Refine.MutableDataValues) }

    infix fun Int.values(f: Level.() -> Unit) { f(Level(this)) }

    inner class Level(val level: Int) {
        val values: Refine.MutableDataValues = data
            as Refine.MutableDataValues

        infix fun String.to(value: String?) {
            values.setValue(level, this, value)
        }
    }
}

infix fun <D: DataValues> D.build(
    f: RefineValuesBuildScope.() -> Unit
): D = this.also { f(RefineValuesBuildScope(this)) }

infix fun <R: Refine> R.build(
    f: RefineValuesBuildScope.() -> Unit
): R = this.also { f(RefineValuesBuildScope(data)) }


var ItemStack.refineData: RefineData? by ModDataComps.REFINE.prop()

fun ItemSettings.refinable(refine: Refine): ItemSettings
= component(ModDataComps.REFINE.value(), RefineData(refine.id))
