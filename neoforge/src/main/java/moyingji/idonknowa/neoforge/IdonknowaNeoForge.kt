package moyingji.idonknowa.neoforge

import com.mojang.logging.LogUtils
import moyingji.idonknowa.Idonknowa
import net.neoforged.fml.common.Mod

@Mod(Idonknowa.MOD_ID)
class IdonknowaNeoForge {
    init {
        LogUtils.getLogger().info("Hello Idonknowa from NeoForge!")
        Idonknowa.init()
    }
}