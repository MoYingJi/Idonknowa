package moyingji.lib.util

//  性 · 能 · 堪 · 忧     我 · 不 · 管  --  喵喵喵?
// 祖传 老好用 函数式编程 -- 使我 Jvm 性能 更上一层 -- 愿你 看懂堆栈 -- 爱来自 Kotlin

// region fun map
// 以下内容为自动生成 请勿更改 不够再加

fun <R1, R2> (() -> R1).map(f: (R1) -> R2): () -> R2 = { f(this()) }
fun <A1, R1, R2> ((A1) -> R1).map(f: (R1) -> R2): (A1) -> R2 = { a1 -> f(this(a1)) }
fun <A1, A2, R1, R2> ((A1, A2) -> R1).map(f: (R1) -> R2): (A1, A2) -> R2 = { a1, a2 -> f(this(a1, a2)) }
fun <A1, A2, A3, R1, R2> ((A1, A2, A3) -> R1).map(f: (R1) -> R2): (A1, A2, A3) -> R2 = { a1, a2, a3 -> f(this(a1, a2, a3)) }

fun <A, T, R> ((A) -> R).mapArg(f: (T) -> A): (T) -> R = { t -> this(f(t)) }
fun <A, T, A2, R> ((A, A2) -> R).mapFirst(f: (T) -> A): (T, A2) -> R = { t, a2 -> this(f(t), a2) }
fun <A1, A, T, R> ((A1, A) -> R).mapSecond(f: (T) -> A): (A1, T) -> R = { a1, t -> this(a1, f(t)) }

// endregion

// region fun currying / uncurrying
// 以下内容为自动生成 请勿更改 不够再加

fun <A1, A2, R> ((A1, A2) -> R).currying(): (A1) -> (A2) -> R = { a1 -> { a2 -> this(a1, a2) } }
fun <A1, A2, A3, R> ((A1, A2, A3) -> R).currying(): (A1) -> (A2) -> (A3) -> R = { a1 -> { a2 -> { a3 -> this(a1, a2, a3) } } }

fun <A1, A2, R> ((A1, A2) -> R).currying(a1: A1): (A2) -> R = { a2 -> this(a1, a2) }
fun <A1, A2, A3, R> ((A1, A2, A3) -> R).currying(a1: A1): (A2) -> (A3) -> R = { a2 -> { a3 -> this(a1, a2, a3) } }

fun <A1, A2, R> ((A1) -> (A2) -> R).uncurrying(): ((A1, A2) -> R) = { a1, a2 -> this(a1)(a2) }
fun <A1, A2, A3, R> ((A1) -> (A2) -> (A3) -> R).uncurrying(): ((A1, A2, A3) -> R) = { a1, a2, a3 -> this(a1)(a2)(a3) }

// endregion