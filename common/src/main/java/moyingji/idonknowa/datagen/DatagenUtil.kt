package moyingji.idonknowa.datagen

import moyingji.idonknowa.Idonknowa.isDatagen
import moyingji.idonknowa.autoreg.RSProvider
import moyingji.idonknowa.platform.isClient

fun isDatagenClient(): Boolean
= isDatagen && isClient()

fun <T> T.datagenClient(f: T.() -> Unit): T
= this.also { if (isDatagenClient()) f() }

inline infix fun <S: RSProvider<*>> S.datagen(f: S.() -> Unit)
: S { if (isDatagen) f(); return this }
