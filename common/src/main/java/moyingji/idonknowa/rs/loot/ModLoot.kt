package moyingji.idonknowa.rs.loot

import moyingji.idonknowa.Id
import net.minecraft.util.context.ContextKeySet

object ModLoot {
    val tableBuilders: MutableList<Triple<LootBuilder, Id, ContextKeySet>> = mutableListOf()
}