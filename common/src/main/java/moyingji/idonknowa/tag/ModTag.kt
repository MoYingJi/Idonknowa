package moyingji.idonknowa.tag

import com.google.common.collect.*
import moyingji.idonknowa.*
import moyingji.idonknowa.Idonknowa.id
import moyingji.idonknowa.core.*
import moyingji.lib.api.autoName
import moyingji.lib.core.*
import moyingji.lib.util.typed
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.data.tags.TagsProvider.TagAppender
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.BlockTags.*
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KProperty

object ModTag {
    val tagKeys: Multimap<TagKey<*>, TagAppender<*>.() -> Unit> = HashMultimap.create()

// region ItemTags
    val itemTag: ModTagger<Item> = tagKey(Registries.ITEM)

// endregion

// region BlockTags
    val blockTag: ModTagger<Block> = tagKey(Registries.BLOCK)

    // 下界合金挖掘等级
    val NEEDS_NETHERITE_TOOL: TagKey<Block> = blockTag("minecraft:needs_netherite_tool")
        .tag(INCORRECT_FOR_DIAMOND_TOOL)
        .tag(INCORRECT_FOR_IRON_TOOL)
        .tag(INCORRECT_FOR_STONE_TOOL)
        .tag(INCORRECT_FOR_GOLD_TOOL)
        .tag(INCORRECT_FOR_WOODEN_TOOL)

// endregion
}

// region Tools
fun <T> tagKey(reg: ResourceKey<out Registry<T>>)
: ModTagger<T> = ModTagger { TagKey.create(reg, it.id) }
fun <T> tagKey(key: TagKey<T>, f: TagAppender<T>.() -> Unit)
{ if (Idonknowa.isDatagen) ModTag.tagKeys.put(key, f.typed()) }
fun <T, R: RegHelper<out T>> R.tag(tag: TagKey<T>): R
= also { listen { tagKey(tag) { add(this@tag.regs.key.typed()) } } }
fun <T, R: ResourceKey<T>> R.tag(tag: TagKey<T>): R
= also { tagKey(tag) { add(this@tag) } }
fun <T, K: TagKey<T>> K.tag(tag: TagKey<T>): K
= this.also { tagKey(tag) { addTag(this@tag) } }

class ModTagger<T>(
    val f: (String) -> TagKey<T>
) : ((String) -> TagKey<T>) by f,
    PropertyDelegateProvider<Any?, PropRead<TagKey<T>>> {
    override fun provideDelegate(thisRef: Any?, property: KProperty<*>): PropRead<TagKey<T>>
    = f(property.autoName(String::lowercase)).propProvider()
}
// endregion