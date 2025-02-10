package moyingji.idonknowa.nbt

import moyingji.idonknowa.core.refine.RefineData

object ModDataComps {
    val REFINE: DataCompS<RefineData> by dataComp(RefineData.CODEC_PAIR)
}
