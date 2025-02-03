package moyingji.idonknowa.datagen

import moyingji.idonknowa.Idonknowa.isDatagen
import moyingji.idonknowa.platform.isClient

fun isDatagenClient(): Boolean
= isDatagen && isClient()

fun <T> T.datagenClient(f: T.() -> Unit): T
= this.also { if (isDatagenClient()) f() }
