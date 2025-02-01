package moyingji.idonknowa.util

import net.minecraft.text.*

fun String.text(): MutableText = this.let(Text::literal)