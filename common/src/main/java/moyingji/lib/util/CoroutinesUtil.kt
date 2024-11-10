package moyingji.lib.util

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

fun <T> Flow<T>.listBlocking(): List<T> = runBlocking { this@listBlocking.toList() }

fun <T, R> T.letBlocking(
    context: CoroutineContext = Dispatchers.Default,
    f: suspend CoroutineScope.(receiver: T) -> R
): R = runBlocking(context) { f(this, this@letBlocking) }

fun <T, R> T.withBlocking(
    context: CoroutineContext = Dispatchers.Default,
    f: suspend T.(scope: CoroutineScope) -> R
): R = runBlocking(context) { f(this@withBlocking, this) }