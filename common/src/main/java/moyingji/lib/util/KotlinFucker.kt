package moyingji.lib.util

import kotlinx.coroutines.*
import java.util.concurrent.CompletableFuture
import java.util.*
import kotlin.reflect.KClass

fun <T: Any> T?.toOptional(): Optional<T> = Optional.ofNullable(this)

fun <T: Any> Array<out T>.typeClassGetter(): KClass<T>
= this::class.java.componentType.kotlin.typed()



typealias CompFuture<R> = CompletableFuture<R>

fun <R> R.runAsync(f: suspend R.() -> Unit): CompFuture<Void>
= let { CompFuture.runAsync    { runBlocking { f(it) } } }
fun <R, T> R.supplyAsync(f: suspend R.() -> T): CompFuture<T>
= let { CompFuture.supplyAsync { runBlocking { f(it) } } }

@JvmName("awaitVoid")
fun CompFuture<Void>.await(): Unit = Unit.also { join() }
fun <R> CompFuture<R>.await(): R = join()

fun <R, T> R.supplyFastest(
    vararg futures: suspend R.() -> T
): CompFuture<T>
= supplyAsync { CompFuture.anyOf(*futures
    .map { supplyAsync(it) }.toTypedArray())
    .typed<CompFuture<T>>().await() }