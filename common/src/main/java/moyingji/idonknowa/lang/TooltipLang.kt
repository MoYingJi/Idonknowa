package moyingji.idonknowa.lang

import moyingji.idonknowa.*
import moyingji.idonknowa.lang.Translations.DISPLAY_DETAILS
import moyingji.idonknowa.util.*
import net.minecraft.ChatFormatting.*
import net.minecraft.client.gui.screens.Screen

object Translations {
    private val IDONKNOWA = TranKey("mod.idonknowa")
    val MOD_MENU_NAME: TranKey = IDONKNOWA.replacePrefix("modmenu.nameTranslation")

    val TEXT: TranKey = IDONKNOWA.replacePrefix("text")

    val ERROR: TranKey = TEXT.suffix("error")

    val REFINE_LEVEL: TranKey = TEXT.suffix("refine_level")
        .withArg("level", "等级")

    val PRESS_TO: TranKey = TEXT.suffix("press_to")
        .withArg("key", "按下的按键")
        .withArg("do", "按下后的动作")

    val DISPLAY_DETAILS: TranKey = TEXT.suffix("display_details")

    val OWNER: TranKey = TEXT.suffix("owner")

    val CURRENT_STATE: TranKey = TEXT.suffix("current_state")
    val STATE_ON: TranKey = CURRENT_STATE.suffix("on")
    val STATE_OFF: TranKey = CURRENT_STATE.suffix("off")
}

fun String.text(): MutableText = Text.literal(this)
fun String.textStyle(style: Formatting): MutableText = text().withStyle(style)

fun pressTo(key: String, doWhat: String): MutableText
= Translations.PRESS_TO.templated
    .replace("key", "$GRAY$key$DARK_GRAY")
    .replace("do", doWhat)
    .value.textStyle(DARK_GRAY)

fun pressShiftToDisplayDetails(
    tooltip: TooltipArgs, assertClient: Boolean = true, f: () -> Unit
) { if ((assertClient || isClientThread()) && !Screen.hasShiftDown())
        pressTo("Shift", DISPLAY_DETAILS.value)
            .also { tooltip += it }
    else f() }
