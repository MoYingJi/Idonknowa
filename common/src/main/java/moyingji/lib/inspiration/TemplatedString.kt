package moyingji.lib.inspiration

class TemplatedString(
    val template: String,
    val spec: String = "@",
    val regex: Regex = Regex("$spec\\{(.*?)}"),
    val values: MutableMap<String, String> = mutableMapOf()
) {
    val value: String get() = template.replace(regex) {
        values[it.groupValues[1]] ?: it.groupValues[1] }

    fun prefix(prefix: String) = TemplatedString(
        "$prefix$template", spec, regex, values)
    fun suffix(suffix: String) = TemplatedString(
        "$template$suffix", spec, regex, values)

    infix operator fun plusAssign(pair: Pair<String, String>) { values += pair }
    operator fun set(key: String, value: String): TemplatedString = this.also { values[key] = value }
    infix fun add(pair: Pair<String, String>): TemplatedString = this.also { values += pair }
    fun add(key: String, value: String): TemplatedString = this.also { values[key] = value }
    infix fun replace(pair: Pair<String, String>): TemplatedString = this.also { values += pair }
    fun replace(key: String, value: String): TemplatedString = this.also { values[key] = value }
}