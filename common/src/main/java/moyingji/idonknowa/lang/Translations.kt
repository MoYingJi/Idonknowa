package moyingji.idonknowa.lang

import moyingji.idonknowa.Idonknowa

object Translations {
    val idonknowa = TransKey(Idonknowa.MOD_ID)

    val msg: TransKey by idonknowa.pre()
}