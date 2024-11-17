package moyingji.idonknowa.world.virtual

import com.google.common.base.Stopwatch
import dev.architectury.utils.Env
import kotlinx.serialization.*
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.json.Json
import moyingji.idonknowa.Idonknowa.currentServer
import moyingji.idonknowa.Idonknowa.id
import moyingji.idonknowa.Idonknowa.info
import moyingji.idonknowa.serialization.KSerJsonData
import moyingji.idonknowa.util.OnlyCallOn
import moyingji.lib.math.*
import moyingji.lib.util.*
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.*
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.saveddata.SavedData.Factory

@OnlyCallOn(Env.SERVER)
object VirtualManager {
    const val REGION_UNIT: Int = RegionUnit.MEDIUM
    object RegionUnit {
        const val CHUNK = 4 // 16
        const val MEDIUM = 5 // 32
        const val REGION = 9 // 512
    }

    @Serializable
    @OptIn(ExperimentalSerializationApi::class)
    data class Data (
        @EncodeDefault var nextIndex: UInt = 0u,
        val freeIndexes: MutableSet<UInt> = mutableSetOf(),
        val occupied: MutableMap<Long, UInt> = mutableMapOf(),
        val regions: MutableMap<UInt, Region> = mutableMapOf(),
    ) : KSerJsonData.State {
        @Transient val lock: Any = this

        fun nextIndex(): UInt = freeIndexes.removeFirstOrNull() ?: nextIndex ++

        fun distribution(sx: UShort = 1u, sz: UShort = 1u): Region {
            require(sx > 0u && sz > 0u)
            info("Distribution Region, Size: $sx, $sz")
            Json.encodeToString(this).also(::info)
            val id = synchronized(lock) { nextIndex() }
            val ov = occupied.keys.map(Long::toVec2i)
            for ((x, z) in spiralSearch(ov, mutateVisited = false)) {
                val fs: MutableList<() -> Unit> = mutableListOf()
                synchronized(lock) {
                    iterableUnit(x, z, sx, sz).all {
                        fs += { occupied += it.toLong() to id }
                        it !in ov
                    }.alsoIf { fs.forEachRemove { it() } }
                }.alsoIf { return Region(id, x, z, sx, sz)
                    .also { regions += id to it } } }
            throw IllegalStateException()
        }
        fun free(rid: UInt) { regions[rid]?.let(::free) }
        fun free(region: Region) { synchronized(lock) {
            if (region.removed || regions[region.index] !== region)
                throw IllegalStateException()
            region.removed = true
            regions -= region.index
            region.iterableUnit().forEach { occupied -= it.toLong() }
            freeIndexes += region.index
        } }
    }

    val worldType: ResourceKey<Level> = ResourceKey.create(Registries.DIMENSION, "virtual_world".id)
    val dataType: Factory<KSerJsonData<Data>> = KSerJsonData.type(Data::class)

    val world: ServerLevel get() = currentServer?.getLevel(worldType)!!

//    val dataState: KSerJsonData<Data> get() = currentServer!!.getLevel(Level.OVERWORLD)!!
//        .dataStorage.computeIfAbsent(dataType, "idonknowa_virtual_world")
    val dataState: KSerJsonData<Data> get() = world.dataStorage
        .computeIfAbsent(dataType, "idonknowa_virtual_world")
    val data: Data by dataState.also { it.setDirty() }

    @Serializable
    @OptIn(ExperimentalSerializationApi::class)
    data class Region (
        // index, start, size 不可变 需要重新申请
        @EncodeDefault @Required val index: UInt,
        // 使用 Long 存储 Vec2i | UInt 存储 Vec2us
        val start: Long, val size: UInt,
        var usedHeight: Long, var usedWidth: UInt,
        // usedHeight, usedWidth 仅用于存储数据 请用 rangeHeight, usedSize
        @EncodeDefault(NEVER) var removed: Boolean = false,
    ) {
        init {
            require(!removed)
            require(validHeight(rangeHeight, world)) // usedHeight
            require(validSize(usedSize)) // usedWidth
        }
        constructor(
            //    start x, z         size x, z
            index: UInt, x: Int, z: Int,
            sx: UShort = 1u, sz: UShort = 1u,
            sh: Int = 0, eh: Int = 0,
            ux: UShort = 0u, uz: UShort = 0u
        ) : this(index,
            (x to z).toLong(),
            (sx to sz).toUInt(),
            (sh to eh).toLong(),
            (ux to uz).toUInt()
        )

        // 下面 坐标/距离 单位为 REGION_UNIT
        @Transient val x: Int = start.pairFirst(); @Transient val z: Int = start.pairSecond() // 区域初始单位坐标 (包含)
        @Transient val sx: UShort = size.pairFirst(); @Transient val sz: UShort = size.pairSecond() // 区域大小

        // 下面 方块坐标 和上面的单位换算为 2^REGION_UNIT
        @Transient val bx: Int = x shl REGION_UNIT; @Transient val bz: Int = z shl REGION_UNIT // 区域初始方块坐标 (包含)
        @Transient val bsx: UInt = ((sx + 1u) shl REGION_UNIT) - 1u
        @Transient val bsz: UInt = ((sz + 1u) shl REGION_UNIT) - 1u
        fun BlockPos.posRegionLocal(): BlockPos = BlockPos(
            this.x - bx, this.y - rangeHeight.s, this.z - bz)
        fun BlockPos.posGlobal(): BlockPos = BlockPos(
            this.x + bx, this.y + rangeHeight.s, this.z + bz)

        val spx: BlockPos get() = BlockPos(bx, rangeHeight.s, bz)
        val upx: BlockPos get() = BlockPos(
            bx + usedSize.first, rangeHeight.e, bz + usedSize.second)
        var rangeHeight: IntRange
            get() = usedHeight.pairFirst()..usedHeight.pairSecond()
            set(value) {
                require(validHeight(value, world))
                usedHeight = (value.s to value.e).toLong() }
        var usedSize: Vec2us
            get() = usedWidth.toVec2us()
            set(value) {
                require(validSize(value))
                usedWidth = value.toUInt() }

        fun validHeight(range: IntRange, world: LevelHeightAccessor): Boolean {
            val worldRange = world.minBuildHeight until world.maxBuildHeight
            return range.s in worldRange && range.e in worldRange
        }
        fun validSize(pairLocal: Vec2us): Boolean {
            val (x, z) = pairLocal
            return x <= bsx && z <= bsz
        }

        fun including(pairLocal: Vec2i) {
            require(pairLocal.all { it in 0..UShort.MAX_VALUE.toInt() })
            val (lx, lz) = pairLocal.map(Int::toUShort)
            var (sx, sz) = usedSize
            if (sx < lx) sx = lx
            if (sz < lz) sz = lz
            usedSize = sx to sz // throw if invalid
        }
        fun including(posGlobal: BlockPos) {
            rangeHeight = rangeHeight.extendTo(posGlobal.y)
            val posLocal = posGlobal.posRegionLocal()
            including(posLocal.x to posLocal.z)
        }

        fun iterableUnit(): Sequence<Vec2i> = iterableUnit(x, z, sx, sz)

        fun clear() { // world CommonLevelAccessor
            !removed || throw IllegalStateException()
            val world = world
            val default = Blocks.AIR.defaultBlockState()
            info("Start Clear Region $index ($spx -> $upx)")
            val timer = Stopwatch.createStarted()
            for (p in BlockPos.betweenClosed(spx, upx))
                world.setBlockAndUpdate(p, default)
            val t = timer.stop().elapsed()
            info("Finished Clear Region $index Use ${t.toMillis()} ms (${t.toSeconds()})")
        }
    }

    fun iterableUnit(x: Int, z: Int, sx: UShort, sz: UShort): Sequence<Vec2i> {
        val ex = x + sx - 1; val ez = z + sz - 1
        return (x..ex).asSequence()
            .flatMap { ix -> (z..ez).asSequence().map { ix to it } }
    }
}