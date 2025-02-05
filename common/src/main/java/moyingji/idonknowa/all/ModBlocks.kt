package moyingji.idonknowa.all

import moyingji.idonknowa.all.block.PrimogemOreBlock
import moyingji.idonknowa.autoreg.*
import moyingji.idonknowa.datagen.LangProvider.C.en
import moyingji.idonknowa.datagen.LangProvider.C.zh
import moyingji.idonknowa.datagen.models.*
import net.minecraft.block.*
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Rarity

object ModBlocks {
    val PRIMOGEM_ORE: RegS<Block> by block(::PrimogemOreBlock) {
        mapColor(MapColor.DEEPSLATE_GRAY)
        strength(50F, 1500F)
        sounds(BlockSoundGroup.DEEPSLATE)
    } withBlockItem {
        settings {
            fireproof()
            rarity(Rarity.RARE)
        } model blockItem
    } tran {
        zh to { "深层原石矿石" }
        en to { "Deepslate Primogem Ore" }
    } model cubeAll
}
