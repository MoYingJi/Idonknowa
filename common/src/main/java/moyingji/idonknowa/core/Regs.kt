package moyingji.idonknowa.core

import moyingji.idonknowa.Id
import moyingji.idonknowa.core.artifact.Artifact
import moyingji.idonknowa.items.LootGacha

object Regs {
    val GACHA: MutableMap<Id, LootGacha> = mutableMapOf()
    val ARTIFACT_EFFECT: MutableMap<Id, Artifact.Effect> = mutableMapOf()
    val ARTIFACT_AFFIX: MutableMap<Id, Artifact.AffixType> = mutableMapOf()
}