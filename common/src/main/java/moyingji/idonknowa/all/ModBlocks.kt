package moyingji.idonknowa.all

import moyingji.idonknowa.all.block.PrimogemOreBlock
import moyingji.idonknowa.autoreg.*
import moyingji.idonknowa.core.Tags
import moyingji.idonknowa.datagen.LangProvider.C.en
import moyingji.idonknowa.datagen.LangProvider.C.zh
import moyingji.idonknowa.datagen.datagen
import moyingji.idonknowa.datagen.drop.*
import moyingji.idonknowa.datagen.model.*
import moyingji.idonknowa.datagen.tag.tag
import net.minecraft.block.*
import net.minecraft.registry.tag.BlockTags
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Rarity

object ModBlocks {
    val PRIMOGEM_ORE: RegS<Block> by block(::PrimogemOreBlock) {
        mapColor(MapColor.DEEPSLATE_GRAY)
        strength(50F, 1500F)
        sounds(BlockSoundGroup.DEEPSLATE)
        requiresTool()
    } withBlockItem {
        settings {
            fireproof()
            rarity(Rarity.RARE)
        } model blockItem
    } tran {
        zh to { "深层原石矿石" }
        en to { "Deepslate Primogem Ore" }
    } datagen {
        this model cubeAll
        this drop self
        this tag BlockTags.PICKAXE_MINEABLE
        this tag Tags.Block.NEEDS_NETHERITE_TOOL
    }
}
