package moyingji.idonknowa.core

import moyingji.idonknowa.Idonknowa
import moyingji.idonknowa.autoreg.RSManager.getReg
import moyingji.idonknowa.datagen.tag.tagTo
import moyingji.idonknowa.util.*
import moyingji.lib.prop.*
import net.minecraft.block.Block
import net.minecraft.registry.tag.*
import net.minecraft.util.Identifier

object Tags {
    init {
        B
    }

    // region OTHER

    fun <T> of(id: Identifier, vararg typeGetter: T)
    : TagKey<T> = TagKey.of(getReg(*typeGetter), id)

    fun <T> delegate(
        reg: RegKeyOutReg<T>,
        namespace: String = Idonknowa.MOD_ID,
        after: TagKey<T>.() -> Unit = {}
    ): PropReadDPA<TagKey<T>>
    = delegateName(String::lowercase) {
        TagKey.of(reg, it.id(namespace)).apply(after) }
    fun <T> delegate(
        namespace: String = Idonknowa.MOD_ID,
        vararg typeGetter: T,
        after: TagKey<T>.() -> Unit = {}
    ): PropReadDPA<TagKey<T>>
    = delegateName(String::lowercase) {
        of(it.id(namespace), *typeGetter).apply(after) }

    // endregion

    object B {
        val NEEDS_NETHERITE_TOOL: TagKey<Block> by delegate {
            tagTo(BlockTags.INCORRECT_FOR_WOODEN_TOOL)
            tagTo(BlockTags.INCORRECT_FOR_STONE_TOOL)
            tagTo(BlockTags.INCORRECT_FOR_IRON_TOOL)
            tagTo(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)
            tagTo(BlockTags.INCORRECT_FOR_GOLD_TOOL)
        }
    }
}
