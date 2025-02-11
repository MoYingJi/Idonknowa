package moyingji.idonknowa.all.item

import moyingji.idonknowa.core.Regs
import moyingji.idonknowa.core.refine.*
import moyingji.idonknowa.datagen.lang.zh
import moyingji.idonknowa.lang.tran
import moyingji.idonknowa.util.*
import net.minecraft.item.Item
import net.minecraft.util.*

@Suppress("ClassName")
class _TestItem(settings: Settings) : Item(
    settings.refinableSelf(DebugRefine)
        .fireproof().rarity(Rarity.EPIC)
        .maxCount(1).glint()
) {
    init { tran {
        zh to "「调试」之仙女棒"
    } }

    object DebugRefine : Refine(37) {
        override val id: Identifier = "debug".id()
        init {
            Regs.REFINE[id] = this
            name.apply {
                zh to "查改之力"
            }
            desc.apply {
                zh to "运用创世之力进行调试\n之 @{ten}@{one}"
            }
            data.build {
                1  values { "ten" to "" }
                10 values { "ten" to "一十" }
                20 values { "ten" to "二十" }
                30 values { "ten" to "三十" }
                for (i in 0..3) {
                    if (i != 0) i*10 values { "one" to "" }
                    i*10+1 values { "one" to "一" }
                    i*10+2 values { "one" to "二" }
                    i*10+3 values { "one" to "三" }
                    i*10+4 values { "one" to "四" }
                    i*10+5 values { "one" to "五" }
                    i*10+6 values { "one" to "六" }
                    i*10+7 values { "one" to "七" }
                    if (i == 3) continue
                    i*10+8 values { "one" to "八" }
                    i*10+9 values { "one" to "九" }
                }
            }
        }
    }
}
