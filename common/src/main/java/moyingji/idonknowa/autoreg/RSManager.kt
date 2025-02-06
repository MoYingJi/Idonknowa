package moyingji.idonknowa.autoreg

import dev.architectury.registry.registries.*
import moyingji.idonknowa.Idonknowa
import moyingji.idonknowa.util.RegKeyOutReg
import moyingji.lib.util.typed
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.registry.RegistryKeys
import kotlin.reflect.KClass

typealias RegS<T> = RegistrySupplier<T>

object RSManager {
    val manager: RegistrarManager = RegistrarManager.get(Idonknowa.MOD_ID)

    val mapReg: MutableMap<KClass<*>, RegKeyOutReg<*>>
    = mutableMapOf(
        Item::class to RegistryKeys.ITEM,
        Block::class to RegistryKeys.BLOCK,
    )
    fun <T> getReg(vararg typeGetter: T): RegKeyOutReg<T>
    = mapReg[typeGetter.javaClass.componentType.kotlin].typed()
}
