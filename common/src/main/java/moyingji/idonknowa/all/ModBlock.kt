package moyingji.idonknowa.all

import moyingji.idonknowa.blocks.*
import moyingji.idonknowa.core.*
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour

typealias BlockSettings = BlockBehaviour.Properties

object ModBlock {
    val PRIMOGEM_ORE: RegS<Block> by PrimogemOre.regHelper

    val ISEKAI_RESEARCH_TABLE: RegS<Block> by IsekaiResearchTable.regHelper
    val QUANTUM_ENTANGLER: RegS<Block> by QuantumEntangler.regHelper
}