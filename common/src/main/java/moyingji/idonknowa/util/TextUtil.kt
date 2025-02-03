package moyingji.idonknowa.util

import net.minecraft.text.*
import net.minecraft.util.Formatting

fun String.text(): MutableText = this.let(Text::literal)
fun String.text(vararg formatting: Formatting)
: MutableText = text().formatted(*formatting)
