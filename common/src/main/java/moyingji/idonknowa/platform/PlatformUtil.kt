package moyingji.idonknowa.platform

import dev.architectury.platform.Platform
import dev.architectury.utils.Env

fun isClient(): Boolean = Platform.getEnvironment() == Env.CLIENT
fun isServer(): Boolean = Platform.getEnvironment() == Env.SERVER