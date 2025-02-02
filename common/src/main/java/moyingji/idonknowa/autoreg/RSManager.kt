package moyingji.idonknowa.autoreg

import dev.architectury.registry.registries.*
import moyingji.idonknowa.Idonknowa

typealias RegS<T> = RegistrySupplier<T>

object RSManager {
    val manager: RegistrarManager = RegistrarManager.get(Idonknowa.MOD_ID)
}