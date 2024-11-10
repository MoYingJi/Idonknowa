package moyingji.idonknowa.datagen

import moyingji.idonknowa.Idonknowa
import moyingji.idonknowa.core.RegHelper

annotation class DatagenOnly

fun <T, R: RegHelper<out T>> R.listenIfDatagen(f: (T) -> Unit)
: R = this.also { if (Idonknowa.isDatagen) listen(f) }