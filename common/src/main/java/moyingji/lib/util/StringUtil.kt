package moyingji.lib.util

fun String.uppercaseFirstChar(): String
= replaceFirstChar(Char::uppercase)

fun String.titleCaseFromConst(): String
= split('_').joinToString { it.uppercaseFirstChar() }
