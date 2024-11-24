package moyingji.idonknowa.blocks

import moyingji.idonknowa.all.*
import moyingji.idonknowa.core.*
import moyingji.idonknowa.datagen.*
import moyingji.idonknowa.rs.loot.dropSelf
import moyingji.idonknowa.rs.loot.dropSilkAndOther
import moyingji.idonknowa.rs.tag.ModTag
import moyingji.idonknowa.rs.tag.tag
import net.minecraft.core.BlockPos
import net.minecraft.tags.BlockTags
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.*
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.material.MapColor
import kotlin.math.*

// region PrimogemOre | 深层原石矿石
class PrimogemOre : Block(BlockSettings.of()
    .mapColor(MapColor.STONE)
    .instrument(NoteBlockInstrument.BASEDRUM)
    .requiresCorrectToolForDrops()
    .strength(37F)
) { companion object { val regHelper: BlockRegHelper = RegHelper
        .block { PrimogemOre() }
        .withSimpleModel()
        .dropSilkAndOther(ModItem.PRIMOGEM)
        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
        .tag(ModTag.NEEDS_NETHERITE_TOOL)
        .blockItem { rarity(Rarity.UNCOMMON).fireResistant() }
    }
    override fun playerDestroy(
        world: Level,
        player: Player,
        blockPos: BlockPos,
        state: BlockState,
        blockEntity: BlockEntity?,
        stack: ItemStack,
    ) { super.playerDestroy(world, player, blockPos, state, blockEntity, stack)
        if (!player.isCreative && !player.isSpectator) player.hurt(
            player.damageSources().magic(), (player.maxHealth + player.absorptionAmount)
                .times(let {
                    val c = 0.27F // 手动随机输入的函数常量
                    val x = (player.health + player.absorptionAmount) / player.maxHealth
                    val y = x / (x + c)
                    return@let y
                } ).let {
                    min(it, player.health + player.absorptionAmount - 1F)
                    // 保证不会死
                } )
        // 当玩家非创造模式时 造成伤害 其初始值为 生命值上限和总生命值的比值 带入函数函数 y=x/(x+c)
    }
}
// endregion

// region IsekaiResearchTable | 异界研究台
class IsekaiResearchTable : Block(BlockSettings.of()
    .mapColor(MapColor.METAL)
    .requiresCorrectToolForDrops()
    .strength(17F)
) { companion object { val regHelper: BlockRegHelper = RegHelper
        .block { IsekaiResearchTable() }
        .withSimpleModel()
        .dropSelf()
        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
        .tag(ModTag.NEEDS_NETHERITE_TOOL)
        .blockItem()
    }
}
// endregion