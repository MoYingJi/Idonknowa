package moyingji.idonknowa.lang

import moyingji.idonknowa.Idonknowa
import moyingji.idonknowa.datagen.lang.*

object Translations {
    val idonknowa = TransKey(Idonknowa.MOD_ID)

    val msg: TransKey by idonknowa.pre()

    val hold_to: TransKey by msg.new {
        zh to "按住 [@{key}] 以@{do}"
        en to "Hold [@{key}] to @{do}"
    }.args("key" to "按键", "do" to "操作")

    val display_details: TransKey by msg.new {
        zh to "显示详细信息"
        en to "Displays details"
    }
}
