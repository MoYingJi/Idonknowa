package moyingji.idonknowa.command

import moyingji.idonknowa.lang.*
import moyingji.idonknowa.world.virtual.VirtualManager
import moyingji.idonknowa.world.virtual.VirtualManager.REGION_UNIT
import moyingji.idonknowa.world.virtual.VirtualManager.data
import moyingji.idonknowa.world.virtual.VirtualManager.world
import moyingji.lib.math.*
import moyingji.lib.util.*
import net.minecraft.ChatFormatting.RED
import net.minecraft.core.BlockPos

object VirtualWorldCommand {
    val vir by literal {
        then(new)
        then(get)

        argument("regionIndex" arg integer(min = 0)
            suggestAsync {
                val data = data
                (0u until data.nextIndex).asSequence()
                    .filter { it !in data.freeIndexes }
                    .map(UInt::toString).forEach(::suggest)
            }
        ) {
            then(info)
            then(tp)
            then(include)
            then(clear)
            then(free)
        }
    }

    // region new
    val new by literal { arguments(
        "sizeX" arg integer(min = 0, max = UShort.MAX_VALUE.toInt()),
        "sizeZ" arg integer(min = 0, max = UShort.MAX_VALUE.toInt()),
    ) { requiresOp(); run {
        val (x, z) = (
            argInt("sizeX") to argInt("sizeZ")
        ).map(Int::toUShort)
        val r = data.distribution(x, z)
        success("[Idonknowa] Region Index: ${r.index}")
    } } }
    // endregion

    // region get
    val get by literal { run {
        world == source.level || fail { dispatcherUnknownCommand().create() }
        val p = source.position
        val l = (p.x to p.z).map(Double::toInt)
            .map { it shr REGION_UNIT }.toLong()
        val i = data.occupied[l]?.toString() ?: "-1"
        success("[Idonknowa] Region Index: $i")
    }; argument("pos", blockPos()) { run {
        val p = argBlockPos("pos")
        val l = (p.x to p.z).map { it shr REGION_UNIT }.toLong()
        val i = data.occupied[l]?.toString() ?: "-1"
        success("[Idonknowa] Region Index: $i")
    } } }
    // endregion

    fun CmdContext.region(): VirtualManager.Region {
        val data = data
        val rs = data.regions
        return synchronized(data.lock) {
            rs[argInt("regionIndex").toUInt()]
                ?: fail { dispatcherUnknownCommand().create() }
        }
    }

    val info by literal { run { with(region()) {
        message("[Idonknowa Region Information]")
        message("Region Index = $index")
        if (removed) message("REMOVED = !!true".textStyle(RED))
        message("Start = |")
        message("  - Region Start = x:$x, z:$z  |  (L:$start)")
        message("  - Block Start = x:$bx, z:$bz")
        message("Size = |")
        message("  - Region Size = x:$sx, z:$sz  |  (L:$size)")
        message("  - Block Size = x:$bsx, z:$bsz")
        message("Used Size = |")
        message("  - Used Horizontally = x:${usedSize.first}, z:${usedSize.second}  |  (L:$usedWidth)")
        message("  - Used Vertically = start:${rangeHeight.s}, endInclusive:${rangeHeight.e}  |  (L:$usedHeight)")
    } } }

    // region tp
    val tpSuccessKey = "commands.teleport.success.location.single".tranKey()
    val tp by literal { argVec3dAbsolute(
        "posRegionLocal"
    ) { requiresOp(); run {
        val r = region()
        val v = with(r) { argVec3dAbsolute("posRegionLocal").posGlobal() }
        val p = player
        p.teleportTo(world, v.x, v.y, v.z, setOf(), p.yRot, p.xRot)
        success(tpSuccessKey.text(p.displayName, v.x, v.y, v.z)
            .append("; (Region ${r.index})"))
    } } }
    // endregion

    // TO.DO 需要向执行者提供命令执行详细信息

    // region include
    val include by literal {
        requiresOp()
        run { includeRegion(player.blockPosition()) }
        then(includeLocal)
        then(includeGlobal)
    }
    val includeLocal by literal("local") {
        argVec3iAbsolute("posRegionLocal") { run {
            val r = region()
            val p = argVec3iAbsolute("posRegionLocal")
            // 如果坐标不在 size 范围内 则自动在 including 报错 捕获并提供信息
            runCatchingFail {
                r.including(p.x to p.z)
                r.rangeHeight.extendTo(p.y)
            }
            success("[Idonknowa] Success")
        } }
    }
    val includeGlobal by literal("global") {
        argBlockPos("pos") { run {
            includeRegion(argBlockPos("pos"))
        } }
    }
    fun CmdContext.includeRegion(pos: BlockPos) {
        // 如果坐标不在 size 范围内 则自动在 including 报错 捕获并提供信息
        runCatchingFail { region().including(pos) }
        success("[Idonknowa] Success")
    }
    // endregion

    val clear by literal { requiresOp(); run {
        region().clear(); success("[Idonknowa] Success")
    }; literal("async") { run { runAsync {
        region().clear(); message("[Idonknowa] Async Clear Finished")
    } } } }

    val free by literal { requiresOp(); run {
        data.free(region()); success("[Idonknowa] Success")
    } }
}