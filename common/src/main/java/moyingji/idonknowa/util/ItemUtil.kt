package moyingji.idonknowa.util

import moyingji.idonknowa.core.RegS
import moyingji.idonknowa.serialization.property
import net.fabricmc.api.EnvType.SERVER
import net.fabricmc.api.Environment
import net.minecraft.core.component.*
import net.minecraft.server.level.*
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.*
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike

private typealias ItemRegS = RegS<out ItemLike>

// region is
infix fun ItemStack.isOf(item: ItemLike): Boolean = this.`is`(item.asItem())
infix fun ItemStack.isIn(key: TagKey<Item>): Boolean = this.`is`(key)
infix fun ItemStack.isOf(item: ItemRegS): Boolean = this isOf item.value()
inline fun <reified T> ItemStack.isOf(): Boolean = item is T
// endregion

// region ItemLike to Stack
val ItemLike.default: ItemStack get() = this.asItem().defaultInstance
val ItemRegS.default: ItemStack get() = this.value().default
fun ItemStack.count(count: Int): ItemStack = this.also { this.count = count }
fun ItemLike.count(count: Int): ItemStack = this.asItem().default.count(count)
fun ItemRegS.count(count: Int): ItemStack = this.value().count(count)
// endregion

fun ItemLike.toIngredient(): Ingredient = Ingredient.of(this)
fun ItemRegS.toIngredient(): Ingredient = this.value().toIngredient()


@Environment(SERVER)
fun ItemStack.hurtAndBreak(amount: Int, living: LivingEntity, onBreak: (Item) -> Unit = {}) {
    // assert: living.level is ServerLevel <=> isServerThread
    hurtAndBreak(amount, living.level() as ServerLevel, living as? ServerPlayer, onBreak) }

fun <T: Any> ItemStack.syncEditing(component: DataComponentType<T>, default: T, editor: (T) -> Unit)
: T? = this.update(component, default) { editor(it); it }

/** 耐久度 (注: 不加判断情况下可为负值!) */
var ItemStack.durability: Int
    get() = maxDamage - damageValue
    set(value) { damageValue = maxDamage - value }


var ItemStack.maxDamageComponent: Int by DataComponents.MAX_DAMAGE.property(0)
var ItemStack.rarityComponent: Rarity by DataComponents.RARITY.property(Rarity.COMMON)
/**
 * 物品是否拥有附魔闪光 默认为`null`情况下由是否附魔决定
 * 优先级高于 [Item.isFoil]
 */
var ItemStack.glintComponent: Boolean? by DataComponents.ENCHANTMENT_GLINT_OVERRIDE.property()