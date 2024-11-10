package moyingji.idonknowa.util

import net.fabricmc.api.*
import net.minecraft.world.entity.player.Player
import java.util.*

@Environment(EnvType.CLIENT)
object ClientUtil {
    // TODO 更好在客户端查找玩家的方法
    val knownClientUuidToPlayerNameMap: MutableMap<UUID, String> = mutableMapOf()
    fun tryGetName(uuid: UUID?, getter: (UUID) -> String?): String {
        if (uuid == null) return "< Unknown >"
        val n = knownClientUuidToPlayerNameMap[uuid]
        if (n != null) return n
        val g = getter(uuid)
        if (g != null) {
            knownClientUuidToPlayerNameMap[uuid] = g
            return g }
        return "< $uuid >"
    }
    fun tryGetName(uuid: UUID?, playerGetter: Player?): String
    = tryGetName(uuid) {
        playerGetter?.level()?.getPlayerByUUID(it)?.gameProfile?.name
    }
}