package moyingji.idonknowa.all.block

import moyingji.idonknowa.util.center
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.World.ExplosionSourceType
import net.minecraft.world.explosion.*
import java.util.*
import kotlin.math.ln

class PrimogemOreBlock(settings: Settings) : Block(settings) {
    /**
     * **深层原石矿石** 在被挖掘后
     * 立即产生一次不破坏方块不伤害玩家不产生火焰的 **爆炸**
     * 随后对玩家造成 `maxHealth - e·ln(health)` 点 **真实伤害**
     */
    override fun afterBreak(
        world: World,
        player: PlayerEntity,
        pos: BlockPos,
        state: BlockState,
        be: BlockEntity?,
        tool: ItemStack
    ) {
        super.afterBreak(world, player, pos, state, be, tool)
        !world.isClient || return

        world as ServerWorld
        world.createExplosion(
            /* entity = */ null,
            Explosion.createDamageSource(world, null),
            AdvancedExplosionBehavior(
                /* destroyBlocks = */ false,
                /* damageEntities = */ false,
                /* knockbackModifier = */ Optional.of(0F),
                /* immuneBlocks = */ Optional.empty()
            ),
            pos.center, 4F, false, ExplosionSourceType.BLOCK
        )

        player as ServerPlayerEntity
        if (player.isInCreativeMode) return
        if (player.health <= Math.E) return
        val hf = Math.E * ln(player.health)
        player.health = hf.toFloat()
    }
}
