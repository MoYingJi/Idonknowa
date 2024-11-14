package moyingji.idonknowa.world.virtual

import com.google.common.base.Stopwatch
import dev.architectury.utils.Env
import kotlinx.serialization.*
import moyingji.idonknowa.Idonknowa
import moyingji.idonknowa.Idonknowa.currentServer
import moyingji.idonknowa.Idonknowa.id
import moyingji.idonknowa.serialization.KSerJsonData
import moyingji.idonknowa.util.OnlyCallOn
import moyingji.lib.math.*
import moyingji.lib.util.*
import moyingji.lib.util.ReflectUtil.removeInvoker
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
    data class Data(
        @EncodeDefault var nextIndex: UInt = 0u,
        val freeIndexes: MutableSet<UInt> = mutableSetOf(),
        val occupied: MutableSet<Long> = mutableSetOf(),
        val regions: MutableMap<UInt, Region> = mutableMapOf(),
    ) {
        @Transient val occupiedPairs: MutableCollection<Vec2i> = occupied.map(Long::toVec2i, Vec2i::toLong)

        @Transient val lock: Any = this

        fun nextIndex(): UInt = freeIndexes.removeFirstOrNull() ?: nextIndex ++

        fun distribution(sx: UShort = 1u, sz: UShort = 1u): Region {
            require(sx > 0u && sz > 0u)
            val id = synchronized(lock) { nextIndex() }
            for ((x, y) in spiralSearch(occupiedPairs)) {
                assert(x to y !in occupiedPairs)
                val r = Region(id, x, y, sx, sz)
                val fs: MutableList<() -> Unit> = mutableListOf()
                var a = false
                synchronized(lock) {
                    a = r.iterableUnit().all {
                        fs += { occupiedPairs += it }
                        it !in occupiedPairs }
                    if (a) fs.removeInvoker().invoke()
                }; if (a) return r }
            throw IllegalStateException()
        }
        fun free(rid: UInt) { regions[rid]?.let(::free) }
        fun free(region: Region) { synchronized(lock) {
            if (regions[region.index] !== region)
                throw IllegalStateException()
            regions -= region.index
            region.iterableUnit().forEach { occupiedPairs -= it }
            freeIndexes += region.index
        } }
    }

    val worldType: ResourceKey<Level> = ResourceKey.create(Registries.DIMENSION, "virtual_world".id)
    val dataType: Factory<KSerJsonData<Data>> = KSerJsonData.type(Data::class)

    val world: ServerLevel get() = currentServer?.getLevel(worldType)!!

    val dataState: KSerJsonData<Data> get() = world.dataStorage.computeIfAbsent(dataType, "idonknowa_virtual_world")
    val data: Data by dataState.also { it.setDirty() }

    @Serializable
    @OptIn(ExperimentalSerializationApi::class)
    data class Region (
        // index, start, size 不可变 需要重新申请
        @EncodeDefault @Required val index: UInt,
        // 使用 Long 存储 Vec2i | UInt 存储 Vec2us
        val start: Long, val size: UInt,
        private var usedHeight: Long, private var usedWidth: UInt
        // usedHeight, usedWidth 仅用于存储数据 请用 rangeHeight, usedSize
    ) {
        init {
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
        @Transient val ex: Int = x + sx - 1; @Transient val ez: Int = z + sz - 1 // 区域结束单位坐标 (包含)

        // 下面 方块坐标 和上面的单位换算为 2^REGION_UNIT
        @Transient val bx: Int = x shl REGION_UNIT; @Transient val bz: Int = z shl REGION_UNIT // 区域初始方块坐标 (包含)
        @Transient val bsx: UInt = ((sx + 1u) shl REGION_UNIT) - 1u
        @Transient val bsz: UInt = ((sz + 1u) shl REGION_UNIT) - 1u
        @Transient val ebx: Int = ((ex + 1) shl REGION_UNIT) - 1 // 区域结束方块坐标 (包含)
        @Transient val ebz: Int = ((ez + 1) shl REGION_UNIT) - 1 // 取下一个单位方块坐标减 1
        fun BlockPos.posRegionLocal(): BlockPos = BlockPos(
            this.x - bx, this.y - rangeHeight.s, this.z - bz)

        val spx: BlockPos get() = BlockPos(bx, rangeHeight.first, bz)
        val epx: BlockPos get() = BlockPos(ebx, rangeHeight.last, ebz)
        val upx: BlockPos get() = BlockPos(
            bx+usedSize.first, rangeHeight.last, bz+usedSize.second)
        var rangeHeight: IntRange
            get() = usedHeight.pairFirst()..usedHeight.pairSecond()
            set(value) { usedHeight = (value.s to value.e).toLong() }
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
            var (sx, sz) = usedSize
            val (lx, lz) = pairLocal.map(Int::toUShort)
            if (sx < lx) sx = lx
            if (sz < lz) sz = lz
            usedSize = sx to sz
        }
        fun including(posGlobal: BlockPos) {
            rangeHeight = rangeHeight.extendTo(posGlobal.y)
            val posLocal = posGlobal.posRegionLocal()
            including(posLocal.x to posLocal.z)
        }

        fun iterableUnit(): Sequence<Vec2i> = (x..ex).asSequence()
            .flatMap { x -> (z..ez).asSequence().map { x to it } }

        fun clear() { // world CommonLevelAccessor
            val world = world
            val default = Blocks.AIR.defaultBlockState()
            Idonknowa.info("Start Clear Region $index ($spx -> $upx)")
            val timer = Stopwatch.createStarted()
            for (p in BlockPos.betweenClosed(spx, upx))
                world.setBlockAndUpdate(p, default)
            val t = timer.stop().elapsed()
            Idonknowa.info("Finished Clear Region $index Use ${t.toMillis()} ms (${t.toSeconds()})")
        }
    }
}