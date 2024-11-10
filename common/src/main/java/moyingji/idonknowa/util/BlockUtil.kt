package moyingji.idonknowa.util

import com.google.common.collect.ImmutableSet
import moyingji.idonknowa.core.RegS
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

fun Block.getPossibleStates(): Set<BlockState>
= ImmutableSet.copyOf(stateDefinition.possibleStates)
fun RegS<out Block>.getPossibleStates(): Set<BlockState>
= value().getPossibleStates()