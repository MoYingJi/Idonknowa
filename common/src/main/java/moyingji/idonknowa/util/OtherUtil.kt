package moyingji.idonknowa.util

import dev.architectury.utils.Env
import moyingji.idonknowa.*
import moyingji.idonknowa.core.RegHelper
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block

annotation class OnlyCallOn(val env: Env)

fun ItemLike.getId(): Id? = RegHelper.manager.get(Registries.ITEM).getId(asItem())
fun Block.getId(): Id? = RegHelper.manager.get(Registries.BLOCK).getId(this)
fun ItemLike.idOrThrow(): Id = getId() ?: throw NoSuchElementException()
fun Block.idOrThrow(): Id = getId() ?: throw NoSuchElementException()