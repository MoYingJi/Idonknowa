package moyingji.idonknowa.poi

import com.google.common.collect.ImmutableSet
import moyingji.idonknowa.*
import moyingji.idonknowa.core.*
import moyingji.idonknowa.mixin.PoiTypesAccessor
import moyingji.idonknowa.tag.tag
import moyingji.idonknowa.util.getPossibleStates
import moyingji.lib.util.isLazyInited
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.sounds.SoundEvent
import net.minecraft.tags.PoiTypeTags
import net.minecraft.world.entity.ai.village.poi.PoiType
import net.minecraft.world.entity.npc.VillagerProfession
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

class SimpleVillagerPoi(
    val id: Id,
    val workSound: SoundEvent,
    lazyBlocks: Lazy<Set<BlockState>> = lazy { setOf() }
) {
    val type: ResourceKey<PoiType> = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, id)
    val blocks: Set<BlockState> by lazyBlocks

    constructor(id: Id, workSound: SoundEvent, states: Set<BlockState>) : this(id, workSound, lazy { states })
    constructor(id: Id, workSound: SoundEvent, block: Block) : this(id, workSound, lazy { block.getPossibleStates() })
    constructor(id: Id, workSound: SoundEvent, reg: RegS<Block>) : this(id, workSound, lazy { reg.get().getPossibleStates() })

    val poiPredicate: (Holder<PoiType>) -> Boolean = { it.`is`(type) }
    val profession: VillagerProfession by lazy { VillagerProfession(
        id.path, poiPredicate, poiPredicate,
        ImmutableSet.of(), ImmutableSet.of(), workSound
    ) }
    val poiType: PoiType by lazy { PoiType(blocks, 1, 1) }

    lateinit var regs: RegS<VillagerProfession>
    lateinit var regsPoi: RegS<PoiType>

    fun reg() = also { reg(this) }
    fun tagAcquirable() = this.also { type.tag(acquirable) }
    fun listen(action: VillagerProfession.() -> Unit) = also { regs.listen(action) }

    companion object {
        val reg = RegHelper.manager.get(Registries.VILLAGER_PROFESSION)
        val regPoi = RegHelper.manager.get(Registries.POINT_OF_INTEREST_TYPE)
        val typeByState: MutableMap<BlockState, Holder<PoiType>> = PoiTypesAccessor.getTypeByState()
        val acquirable = PoiTypeTags.ACQUIRABLE_JOB_SITE
        fun reg(poi: SimpleVillagerPoi): Pair<RegS<PoiType>, RegS<VillagerProfession>> {
            if (poi::profession.isLazyInited() ||
                poi::poiType.isLazyInited() ||
                poi::blocks.isLazyInited())
                throw IllegalStateException()
            val r = reg.register(poi.id) { poi.profession }
            poi.regs = r
            val p = regPoi.register(poi.id) { poi.poiType }
            p.listen {
                val holder = regPoi.getHolder(poi.id) ?:
                    return@listen Unit.also { Idonknowa.error("Failed write poi typeByState") }
                poi.blocks.forEach { typeByState += it to holder } }
            poi.regsPoi = p
            return p to r
        }
    }
}