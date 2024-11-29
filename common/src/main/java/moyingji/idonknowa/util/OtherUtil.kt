package moyingji.idonknowa.util

import dev.architectury.utils.Env
import moyingji.idonknowa.Id
import moyingji.idonknowa.core.RegHelper
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block

annotation class OnlyCallOn(val env: Env)

fun ItemLike.getId(): Id? = RegHelper.manager.get(Registries.ITEM).getId(asItem())
fun Block.getId(): Id? = RegHelper.manager.get(Registries.BLOCK).getId(this)
fun ItemLike.idOrThrow(): Id = getId() ?: throw NoSuchElementException()
fun Block.idOrThrow(): Id = getId() ?: throw NoSuchElementException()


fun Id.withoutNamespaces(vararg namespaces: String)
: String = if (this.namespace in namespaces)
    this.path else this.toString()
fun Id.withoutDefaultNamespace(): String = withoutNamespaces("minecraft")