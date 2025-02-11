package moyingji.idonknowa.core.refine

import moyingji.idonknowa.autoreg.*
import moyingji.idonknowa.core.refine.Refine.DataValues
import moyingji.idonknowa.nbt.*
import moyingji.idonknowa.util.listen
import net.minecraft.item.*

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

// region Refine Upgrading Slot Reg

fun <T: ItemConvertible> T.regRefinable(): T
= this.also { RefineTempItem.refinable += it }
fun <T: ItemConvertible> T.regRefineAdd(): T
= this.also { RefineTempItem.refine_addition += it }
fun <T: ItemConvertible> T.regRefineSelf(): T
= this.also { regRefinable(); regRefineAdd() }

fun <R: RegS<out ItemConvertible>> R.regRefinable(): R
= this.also { listen { it.regRefinable() } }
fun <R: RegS<out ItemConvertible>> R.regRefineAdd(): R
= this.also { listen { it.regRefineAdd() } }
fun <R: RegS<out ItemConvertible>> R.regRefineSelf(): R
= this.also { listen { it.regRefineSelf() } }

fun <R: RSProvider<I>, I: ItemConvertible> R.regRefinable(): R
= listen { it.regRefinable() }
fun <R: RSProvider<I>, I: ItemConvertible> R.regRefineAdd(): R
= listen { it.regRefineAdd() }
fun <R: RSProvider<I>, I: ItemConvertible> R.regRefineSelf(): R
= listen { it.regRefineSelf() }

fun ItemSettings.refinable(refine: Refine): ItemSettings
= component(ModDataComps.REFINE.value(), RefineData(refine.id))
    .listen { it.regRefinable() }
fun ItemSettings.refinableSelf(refine: Refine): ItemSettings
= component(ModDataComps.REFINE.value(), RefineData(refine.id))
    .listen { it.regRefineSelf() }

// endregion
