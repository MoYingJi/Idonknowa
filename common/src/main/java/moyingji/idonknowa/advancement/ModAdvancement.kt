package moyingji.idonknowa.advancement

import moyingji.idonknowa.all.ModItem
import net.minecraft.advancements.AdvancementHolder

object ModAdvancement {
    val advancements: MutableList<AdvancementHolder> = mutableListOf()

    val root: AdvHolder by AdvBuilder {
        this background "textures/block/primogem_ore.png"
        criterion got ModItem.PRIMOGEM
        noToast(); noAnnounce()
    }

    val got_primogem: AdvHolder by AdvBuilder(root) { criterion got ModItem.PRIMOGEM }
    val got_mora: AdvHolder by AdvBuilder(root) { criterion got ModItem.MORA }
}