package moyingji.idonknowa.core

import com.google.common.collect.*
import moyingji.idonknowa.Id
import moyingji.idonknowa.core.artifact.Artifact
import moyingji.idonknowa.items.LootGacha
import java.math.BigDecimal

object Regs {
    val GACHA: MutableMap<Id, LootGacha> = mutableMapOf()

    val ARTI_EFFECT: MutableMap<Id, Artifact.Effect> = mutableMapOf()
    val ARTI_AFFIX: MutableMap<Id, Artifact.AffixType> = mutableMapOf()
    val ARTI_AFFIX_VALUE: Multimap<Id, BigDecimal> = HashMultimap.create()
    val ARTI_AFFIX_WEIGHT: MutableMap<Id, UShort> = mutableMapOf() // null -> 100u
}