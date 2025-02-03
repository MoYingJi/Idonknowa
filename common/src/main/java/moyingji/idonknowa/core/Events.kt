package moyingji.idonknowa.core

import arrow.core.partially1
import dev.architectury.event.Event
import dev.architectury.event.EventFactory.createLoop
import dev.architectury.utils.Env
import dev.architectury.utils.Env.CLIENT
import moyingji.idonknowa.lang.*
import moyingji.idonknowa.util.*
import net.minecraft.client.gui.screen.Screen
import net.minecraft.util.Formatting

typealias EventAccept<T> = Event<(T) -> Unit>

object Events {
    @Retention(AnnotationRetention.BINARY)
    annotation class RunOnly(val value: Env)

    object ItemTooltip {
        @RunOnly(CLIENT) val BEFORE_ITEM: EventAccept<TooltipArgs> = createLoop()
        @RunOnly(CLIENT) val AFTER_ITEM: EventAccept<TooltipArgs> = createLoop()
    }

    fun default() {
        // region default desc tooltip
        fun tooltipDefaultDesc(
            shift: Boolean = true,
            suffix: String,
            tooltip: TooltipArgs,
        ) {
            val key = tooltip.item.translationKey.tran()
                .suffix("idonknowa.desc").suffix(suffix)
                .apply { if (shift) suffix("shift") }
            if (key.hasLines) {
                Translations.detailsShift(
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
        ItemTooltip.BEFORE_ITEM.register(tipDefDescF.partially1("before"))
        ItemTooltip.AFTER_ITEM.register(tipDefDescF.partially1("after"))
        // endregion
    }
}
