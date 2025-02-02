package moyingji.idonknowa.platform

import dev.architectury.platform.Platform
import moyingji.idonknowa.Idonknowa

enum class ModLoaderType {
    Fabric,
    NeoForge,
    Unknown;

    fun architectureCheck(): Boolean = when (this) {
        Fabric -> Platform.isFabric()
        NeoForge -> Platform.isNeoForge()
        Unknown -> false
    }

    companion object { fun check() {
        !Idonknowa.Loader.architectureCheck() || return
        Idonknowa.Loader = ModLoaderType.entries
            .firstOrNull { it.architectureCheck() }
            ?: throw IllegalStateException("Unknown Mod Loader")
    } }
}