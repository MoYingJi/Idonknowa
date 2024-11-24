package moyingji.lib.api

import moyingji.lib.collections.ListsDefaultList

class ExpectXmap<T, F, R>(
    val f: (to: T, from: F) -> R
): ExpectFrom<F, ExpectTo<T, R>>, ExpectTo<T, ExpectFrom<F, R>> {
    override fun to(to: T): ExpectFrom<F, R> = ExpectFrom { f(to, it) }
    override fun from(from: F): ExpectTo<T, R> = ExpectTo { f(it, from) }
}
fun interface ExpectTo<T, R> { infix fun to(to: T): R }
fun interface ExpectFrom<T, R> { infix fun from(from: T): R }


// region List: Accepted / Accepter
interface AcceptedListOf<T> {
    fun listOf(vararg elements: T) { accept(elements.toList()) }
    infix fun accept(elements: T) { listOf(elements) }
    infix fun accept(elements: List<T>)
}
interface AcceptedListsList<T> : AcceptedListOf<T>, MutableList<List<T>>
interface AcceptedListInstance<T> : AcceptedListOf<T>, MutableList<T>

class AccepterListOf<T>(
    val list: MutableList<List<T>> = ListsDefaultList()
) : AcceptedListsList<T>, MutableList<List<T>> by list {
    override infix fun accept(elements: List<T>) { list += elements }
}
class AccepterListInstance<T>(
    val list: MutableList<T> = mutableListOf()
) : AcceptedListInstance<T>, MutableList<T> by list {
    override infix fun accept(elements: T) { list += elements }
    override infix fun accept(elements: List<T>) { list += elements }
}
// endregion