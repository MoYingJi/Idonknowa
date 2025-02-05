package moyingji.idonknowa.core

import moyingji.idonknowa.util.id
import net.minecraft.block.Block
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier

typealias BlockTag = TagKey<Block>

object Tags {
    fun keyBlock(id: Identifier): BlockTag = TagKey.of(RegistryKeys.BLOCK, id)

    object Block {
        val NEEDS_NETHERITE_TOOL: BlockTag = keyBlock("needs_tool_level_4".id("fabric"))
        // TODO ↑
    }
}
