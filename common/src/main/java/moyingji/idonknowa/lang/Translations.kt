package moyingji.idonknowa.lang

import moyingji.idonknowa.Idonknowa
import moyingji.idonknowa.datagen.LangProvider.C.en
import moyingji.idonknowa.datagen.LangProvider.C.zh
import moyingji.idonknowa.util.text
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.*
import net.minecraft.util.Formatting

object Translations {
    val idonknowa = TransKey(Idonknowa.MOD_ID)

    val msg: TransKey by idonknowa.pre()

    val hold_to: TransKey by msg.new {
        zh to { "按住 [{key}] 以{do}" }
        en to { "Hold [{key}] to {do}" }
    }.args("key" to "按键", "do" to "操作")

    val display_details: TransKey by msg.new {
        zh to { "显示详细信息" }
        en to { "Displays details" }
    }

    // region detailsShift 实现了 Tooltip 里最常见的 Shift 显示详细信息
    fun detailsShift(
        backColor: Formatting = Formatting.DARK_GRAY,
        shiftColor: Formatting = Formatting.GRAY,
    ): MutableText = hold_to.tempValue.add(
        "key" to "${shiftColor}Shift${backColor}",
        "do" to display_details.value
    ).prefix("$backColor").value.text()

    fun <T: MutableList<Text>> detailsShift(
        tooltip: T,
        backColor: Formatting = Formatting.DARK_GRAY,
        actionColor: Formatting? = Formatting.GRAY,
        shiftColor: Formatting =
            if (actionColor == null)
                Formatting.DARK_GRAY else Formatting.WHITE,
        hasShiftDown: () -> Boolean = Screen::hasShiftDown,
        action: T.() -> Unit
    ) { if (hasShiftDown()) {
            if (actionColor != null)
                tooltip += detailsShift(backColor, actionColor)
            action(tooltip)
        } else tooltip += detailsShift(backColor, shiftColor) }
    // endregion
}
