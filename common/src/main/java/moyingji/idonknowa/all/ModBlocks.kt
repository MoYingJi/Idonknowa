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
        settings.fireproof().rarity(Rarity.RARE)
        this model blockItem
    } tran {
        zh to "深层原石矿石" itemAutoDesc """
                一种稀有的矿石，一般不容易被发现，你需要有一把好镐才能挖动
            """.trimIndent()
        en to "Deepslate Primogem Ore" itemAutoDesc """
                A rare ore, which is hard to find
                you need a good pickaxe to mine it.
            """.trimIndent()
    } datagen {
        this model cubeAll
        this drop self
        this tag BlockTags.PICKAXE_MINEABLE
        this tag Tags.B.NEEDS_NETHERITE_TOOL
    }
}
