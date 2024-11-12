package moyingji.lib.util

class ExpectXmap<F, T, R>(
    val f: (from: F, to: T) -> R
): ExpectFrom<F, ExpectTo<T, R>>, ExpectTo<T, ExpectFrom<F, R>> {
    override fun from(from: F): ExpectTo<T, R> = ExpectTo { f(from, it) }
    override fun to(to: T): ExpectFrom<F, R> = ExpectFrom { f(it, to) }
}

fun interface ExpectFrom<T, R> { infix fun from(from: T): R }
fun interface ExpectTo<T, R> { infix fun to(to: T): R }