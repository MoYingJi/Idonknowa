package moyingji.lib.util

val Any?.str: String get() = this.toString()

fun String.prefix(prefix: String): String = "$prefix$this"
fun String.suffix(suffix: String): String = "$this$suffix"

fun String.titlecase(): String = this.replaceFirstChar(Char::titlecase)