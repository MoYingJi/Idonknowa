package moyingji.idonknowa.core.refine

import moyingji.idonknowa.core.Regs
import moyingji.lib.util.firstKeyOf
import net.minecraft.util.Identifier

open class Refine(
    val maxRefineLevel: Int = 5
) {
    val id: Identifier by lazy { Regs.REFINE.firstKeyOf(this) }

    val valuesRefine: Array<MutableMap<String, String>>
    = Array(maxRefineLevel) { mutableMapOf() }

    fun getValue(level: Int, key: String): String? {
        return if (level < 0 || level >= maxRefineLevel) null
        else valuesRefine[level][key] ?: getValue(level - 1, key)
    }
}
