package moyingji.idonknowa.util

import arrow.core.partially1
import moyingji.idonknowa.core.Events
import moyingji.idonknowa.lang.Translations.display_details
import moyingji.idonknowa.lang.Translations.hold_to
import moyingji.idonknowa.lang.tran
import net.minecraft.client.gui.screen.Screen
import net.minecraft.item.Item
import net.minecraft.text.*
import net.minecraft.util.Formatting

object TooltipUtil {
    /**
     * 使用 [Events.ItemTooltip.BEFORE_ITEM] 和 [Events.ItemTooltip.AFTER_ITEM]
     * 使得无需修改 [Item] 类 就可以默认检查对应本地化键并显示描述
     * 已经在 [Events.default] 中被默认调用
     *
     * 自动检查以下本地化键 (`xxx` 代表物品名称对应的本地化键)
     * - `xxx.idonknowa.desc.shift.before` (多行, 提示按住 Shift)
     * - `xxx.idonknowa.desc.shift.after` (多行, 提示按住 Shift)
     * - `xxx.idonknowa.desc.before` (多行或单行, 无需 Shift)
     * - `xxx.idonknowa.desc.after` (多行或单行, 无需 Shift)
     */
    fun regSimpleAutoDesc() {
        fun tooltipDefaultDesc(
            shift: Boolean = true,
            suffix: String,
            tooltip: TooltipArgs,
        ) {
            val key = tooltip.item.translationKey.tran()
                .suffix("idonknowa.desc").suffix(suffix)
                .apply { if (shift) suffix("shift") }
            if (key.hasLines) {
                detailsShift(
                    tooltip = tooltip,
                    hasShiftDown = { shift && Screen.hasShiftDown() }
                ) { key.lines.lines().forEach {
                    val color = Formatting.GRAY
                    tooltip += it.text(color)
                } }
            } else if (!shift && key.hasValue) {
                // ↑ 仅允许非 shift 单行介绍 (单行我要这 shift 干嘛)
                val color = Formatting.GRAY
                tooltip += key.value.text(color)
            }
            if (shift) tooltipDefaultDesc(false, suffix, tooltip)
        }
        val tipDefDescF = ::tooltipDefaultDesc.partially1(true)
        Events.ItemTooltip.BEFORE_ITEM
            .register(tipDefDescF.partially1("before"))
        Events.ItemTooltip.AFTER_ITEM
            .register(tipDefDescF.partially1("after"))
    }
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
    shiftColor: Formatting = if (actionColor == null)
        Formatting.DARK_GRAY else Formatting.WHITE,
    hasShiftDown: () -> Boolean = Screen::hasShiftDown,
    action: T.() -> Unit
) { if (hasShiftDown()) {
    if (actionColor != null)
        tooltip += detailsShift(backColor, actionColor)
    action(tooltip)
} else tooltip += detailsShift(backColor, shiftColor) }
// endregion
