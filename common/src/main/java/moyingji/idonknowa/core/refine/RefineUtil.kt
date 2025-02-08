package moyingji.idonknowa.core.refine

class RefineValuesBuildScope(val refine: Refine) {
    infix fun Int.values(f: Level.() -> Unit) { f(Level(this)) }

    inner class Level(level: Int) {
        val map = refine.valuesRefine[level - 1]

        infix fun String.to(value: String?) {
            if (value != null) map[this] = value
            else map.remove(this) }
    }
}

infix fun <R: Refine> R.build(
    f: RefineValuesBuildScope.() -> Unit
): R = this.also { f(RefineValuesBuildScope(this)) }
