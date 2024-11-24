package moyingji.idonknowa.all

import dev.architectury.registry.CreativeTabRegistry
import moyingji.idonknowa.core.*
import moyingji.idonknowa.lang.*
import moyingji.idonknowa.util.default
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.level.ItemLike

private typealias TabRegS = RegS<CreativeModeTab>

private typealias DisplayParam = CreativeModeTab.ItemDisplayParameters
private typealias Output = CreativeModeTab.Output

object ModTab {
    class TabRegHelper(initializer: () -> CreativeModeTab) : RegHelper<CreativeModeTab>(Registries.CREATIVE_MODE_TAB, initializer)

    operator fun Output.plusAssign(reg: RegS<out ItemLike>) { this.accept(reg.value()) }

    val tranKey: TranKey = "category.idonknowa".tranKey()

    @Suppress("UnstableApiUsage")
    val MOD_TAB: TabRegS by TabRegHelper {
        CreativeTabRegistry.create {
            it.title(tranKey.text())
            it.icon { ModItem.PRIMOGEM.default }
            it.displayItems { _: DisplayParam, t: Output ->

                t += ModItem.PRIMOGEM
                t += ModBlock.PRIMOGEM_ORE
                t += ModItem.MORA
                t += ModItem.INTERTWINED_FATE
                t += ModItem.REFINE_TEMP

            }
        }
    }
}