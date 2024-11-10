package moyingji.idonknowa.util

import dev.architectury.utils.Env
import moyingji.idonknowa.Idonknowa.currentServer
import moyingji.idonknowa.Idonknowa.threadClient
import moyingji.idonknowa.Idonknowa.threadServer
import moyingji.lib.util.isTrue
import net.minecraft.client.Minecraft.getInstance
import java.lang.Thread.currentThread

fun platform(): Env = platformOrNull() ?: throw IllegalStateException()
fun platformOrNull(): Env? {
    val thread = currentThread()
    // 直接判断
    if (thread == threadServer) return Env.SERVER
    if (thread == threadClient) return Env.CLIENT
    // 名称匹配
    if (thread.name == "Server thread") return Env.SERVER
    if (thread.name == "Render Thread") return Env.CLIENT
    // 重试
    if (isServerThread()) return Env.SERVER
    if (isClientThread()) return Env.CLIENT
    // 其他
    return null
}
// 使用 [BlockableEventLoop.isSameThread] 判断 当类不存在时 会引发异常
fun isServerThread(): Boolean = threadServer == currentThread()
    || currentThread().name == "Server thread"
    || currentThread().name == "main"
    || runCatching { currentServer?.isSameThread }.isTrue()
fun isClientThread(): Boolean = threadClient == currentThread()
    || currentThread().name == "Render Thread"
    || runCatching { getInstance().isSameThread }.isTrue()

fun Env?.isServer(): Boolean = this == Env.SERVER
fun Env?.isClient(): Boolean = this == Env.CLIENT
inline fun whenServerThread(action: () -> Unit) { if (isServerThread()) action() }
inline fun whenClientThread(action: () -> Unit) { if (isClientThread()) action() }
