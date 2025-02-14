package moyingji.idonknowa.util

import arrow.core.partially1
import moyingji.idonknowa.core.Events
import moyingji.idonknowa.lang.*
import moyingji.idonknowa.lang.Translations.display_details
import moyingji.idonknowa.lang.Translations.hold_to
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
     * - `xxx.idonknowa.desc.before.shift` (多行或单行, 提示按住 Shift)
     * - `xxx.idonknowa.desc.after.shift` (多行或单行, 提示按住 Shift)
     * - `xxx.idonknowa.desc.before` (多行或单行, 无需 Shift)
     * - `xxx.idonknowa.desc.after` (多行或单行, 无需 Shift)
     */
    fun regSimpleAutoDesc() {
        fun tooltipDefaultDesc(
            shift: Boolean = true,
            suffix: String,
            tooltip: TooltipArgs,
        ) {
            val key = getAutoDescKey(
                tooltip.item.transKey(), shift, suffix)
            if (key.hasValue) {
                detailsShift(
                    tooltip = tooltip,
                    hasShiftDown = { shift && Screen.hasShiftDown() }
                ) { key.value.lines().forEach {
                    val color = Formatting.GRAY
                    tooltip += it.text(color)
                } }
            }
            if (shift) tooltipDefaultDesc(false, suffix, tooltip)
        }
        val tipDefDescF = ::tooltipDefaultDesc.partially1(true)
        Events.ItemTooltip.BEFORE_ITEM
            .register(tipDefDescF.partially1("before"))
        Events.ItemTooltip.AFTER_ITEM
            .register(tipDefDescF.partially1("after"))
    }

    fun getAutoDescKey(
        original: TransKey,
        isNeedsShift: Boolean,
        at: String
    ): TransKey = original
        .suffix("idonknowa.desc").suffix(at)
        .suffix(if (isNeedsShift) "shift" else "")
        .multilines()


    /**
     * 使用 [Events.ItemTooltip.AFTER_ITEM] 注册
     * 使得带有 [TooltipProcessor] 的 数据组件 自动调用
     */
    fun regAutoTooltipDataComp() {
        Events.ItemTooltip.AFTER_ITEM.register {
            it.stack.components.forEach { comp ->
                val value: Any = comp.value
                if (value is TooltipProcessor)
                    value.processTooltip(it)
            }
        }
    }
}

// 我知道有 TooltipAppender 但那个只接受 Consumer<Text> 而没啥信息
// 而且自己搞一个也方便自动处理
interface TooltipProcessor {
    fun processTooltip(tooltip: TooltipArgs) {}
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
    actionColor: Formatting? = Formatting.WHITE,
    shiftColor: Formatting = Formatting.GRAY,
    hasShiftDown: () -> Boolean = Screen::hasShiftDown,
    condition: () -> Boolean = { true },
    action: T.() -> Unit
) {
    if (!condition()) {
        action(tooltip); return
    }
    if (hasShiftDown()) {
        if (actionColor != null)
            tooltip += detailsShift(backColor, actionColor)
        action(tooltip)
    } else tooltip += detailsShift(backColor, shiftColor)
}
// endregion
