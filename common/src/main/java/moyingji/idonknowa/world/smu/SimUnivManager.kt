package moyingji.idonknowa.world.smu

import dev.architectury.utils.Env
import kotlinx.serialization.Serializable
import moyingji.idonknowa.serialization.KSerJsonData
import moyingji.idonknowa.util.OnlyCallOn
import moyingji.idonknowa.world.virtual.VirtualManager
import moyingji.idonknowa.world.virtual.VirtualManager.world
import moyingji.lib.util.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.saveddata.SavedData.Factory
import kotlin.uuid.*

@OnlyCallOn(Env.SERVER)
object SimUnivManager {
    @Serializable
    class Data (
        val raidsRegionId: MutableMap<UInt, RaidData> = mutableMapOf(),
    ) {
        fun valid(clear: Boolean = false, validChild: Boolean = false): Boolean {
            // id: UInt -> (Region? -> id: UInt, data: RaidData)
            // 这里可以通过 Region? == null 取得其他值从而方便后续 validChild 或删除操作
            val seq = raidsRegionId
                .asSequence().map { (u, d) ->
                    Triple(VirtualManager.dataState.data.regions[u], u, d) }
            // validRaid 判断函数
            fun validRaid(
                tr: Triple<VirtualManager.Region?, UInt, RaidData>
            ): Boolean {
                tr.first != null || return false
                tr.second == tr.third.regionId || return false
                !validChild && tr.third.valid() || return false
                return true }
            // 全部 Entry 均有效 返回 true
            if (seq.all(::validRaid)) return true
            // 清理 raidsRegionId 中 无效 Entry
            // v.map(::not) 对此函数结果取反 筛出无效 Entry
            if (!clear) return false; seq
                .filter(::validRaid.map(Boolean::not)).map { it.second }
                .forEach(raidsRegionId::remove)
            return false
        }
    }

    @Serializable
    @OptIn(ExperimentalUuidApi::class)
    data class RaidData (
        val regionId: UInt,
        val playersData: MutableMap<Uuid, RaidEntrance> = mutableMapOf(),
    ) {
        fun valid(clear: Boolean = false): Boolean {
            // 逻辑同上 [Data.valid]
            val seq = playersData
                .asSequence().map { (u, _) -> u.toJavaUuid() to u }
                .map { (u, k) -> world.getPlayerByUUID(u) to k }
            if (seq.all { it.first != null }) return true
            if (!clear) return false; seq
                .filter { it.first == null }
                .map { it.second }
                .forEach(playersData::remove)
            return false
        }
    }

    @Serializable
    data class RaidEntrance (
        val enterWorldId: String,
        val enterPos: ATriple<Double>,
        val enterFace: APair<Float>,
    ) {
        fun teleport(player: ServerPlayer) { player.teleportTo(world,
            enterPos.first, enterPos.second, enterPos.third,
            enterFace.first, enterFace.second) }
    }

    val dataType: Factory<KSerJsonData<Data>> = KSerJsonData.type(Data::class)
    val dataState: KSerJsonData<Data> get() = world.dataStorage
        .computeIfAbsent(dataType, "idonknowa_simulated_universe")
    val data: Data by dataState.also { it.setDirty() }
}