package moyingji.idonknowa.items

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import moyingji.idonknowa.*
import moyingji.idonknowa.Idonknowa.id
import moyingji.idonknowa.all.*
import moyingji.idonknowa.core.*
import moyingji.idonknowa.datagen.withFlatModel
import moyingji.idonknowa.gui.*
import moyingji.idonknowa.items.WishItem.Result.Companion.resultItems
import moyingji.idonknowa.items.WishItem.Result.Companion.resultStar
import moyingji.idonknowa.lang.*
import moyingji.idonknowa.serialization.*
import moyingji.idonknowa.util.*
import moyingji.idonknowa.loot.*
import moyingji.lib.util.*
import moyingji.libsr.games.Wish
import moyingji.libsr.games.Wish.*
import net.minecraft.ChatFormatting.*
import net.minecraft.core.BlockPos
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.*
import net.minecraft.util.Mth.clamp
import net.minecraft.world.*
import net.minecraft.world.entity.*
import net.minecraft.world.entity.player.*
import net.minecraft.world.inventory.*
import net.minecraft.world.inventory.ClickAction.SECONDARY
import net.minecraft.world.item.*
import net.minecraft.world.item.component.CustomModelData
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.loot.*
import java.lang.Math.round
import java.util.Optional

typealias LootGacha = Gacha<LootTable>
typealias UpLootGacha = UpGacha<LootTable>

class WishItem : Item(ItemSettings()
    .stacksTo(1)
    .component(WISH_CONSUMABLE.value(), ModItem.INTERTWINED_FATE.toIngredient())
), ItemUsingUtil, TooltipUtil {
    companion object {
        val regHelper = RegHelper
            .item { WishItem() }
            .withFlatModel()

        // region Serialization / Network Sync
        val WISH_DATA_CODEC: Codec<WishData> = RecordCodecBuilder.mapCodec {
            b -> b.group(
                ModCodecs.USHORT .optionalFieldOf("last5")   .forGetter(WishData::last5   .mapOptional()),
                ModCodecs.USHORT .optionalFieldOf("last4")   .forGetter(WishData::last4   .mapOptional()),
                ModCodecs.UBYTE  .optionalFieldOf("lastUp5") .forGetter(WishData::lastUp5 .mapOptional()),
                ModCodecs.UBYTE  .optionalFieldOf("lastUp4") .forGetter(WishData::lastUp4 .mapOptional()),
            ).apply(b, ::dataOptional) }.codec()
        fun dataOptional(a: Optional<UShort>, b: Optional<UShort>, c: Optional<UByte>, d: Optional<UByte>)
        : WishData = WishData().apply {
            a.ifPresent { last5 = it }
            b.ifPresent { last4 = it }
            c.ifPresent { lastUp5 = it }
            d.ifPresent { lastUp4 = it } }
        val WISH_DATA_CODEC_STREAM: SCodec<WishData> = StreamCodec.composite(
            ModCodecs.USHORT_S, { it.last5 },
            ModCodecs.USHORT_S, { it.last4 },
            ModCodecs.UBYTE_S,  { it.lastUp5 },
            ModCodecs.UBYTE_S,  { it.lastUp4 },
            ::WishData)
        // endregion

        val WISH_DATA_NBT by nbtType(WISH_DATA_CODEC, WISH_DATA_CODEC_STREAM)
        val WISH_GACHA_NBT by nbtType(ModCodecs.ID_P)
        val WISH_REMAIN_NBT by nbtType(ModCodecs.UINT_P)
        val WISH_CONSUMABLE by nbtType(Ingredient.CODEC, Ingredient.CONTENTS_STREAM_CODEC)
        val WISH_LAST_CONSUMABLE by nbtType(ModCodecs.UINT_P)

        private var ItemStack.wishData by WISH_DATA_NBT.property(WishData())
        private var ItemStack.wishGacha by WISH_GACHA_NBT.property()
        private var ItemStack.wishRemain by WISH_REMAIN_NBT.property(0u)
        private var ItemStack.wishConsumable by WISH_CONSUMABLE.property()
        private var ItemStack.wishLastConsumable by WISH_LAST_CONSUMABLE.property(1u)

        init { DefaultGacha }
    }

    val GACHA_KEY by lazyKeySuffix("gacha")
    val CONSUMABLE_KEY by lazyKeySuffix("consumable")

    override fun appendTooltip(tooltip: TooltipArgs) {
        super.appendTooltip(tooltip)
        val g = tooltip.stack.wishGacha
            ?.let { if (Regs.GACHA.containsKey(it)) it else null }
            ?.toLanguageKey("gacha")
            ?.tranKey()?.text()
            ?: Translations.ERROR.text().withStyle(RED)
        Text.empty()
            .append(GACHA_KEY.text().withStyle(DARK_GRAY))
            .append(": ".textStyle(DARK_GRAY))
            .append(g)
            .also { tooltip += it }
        Text.empty()
            .append(CONSUMABLE_KEY.text().withStyle(DARK_GRAY))
            .append(": ".textStyle(DARK_GRAY))
            .append(tooltip.stack.wishRemain.toString())
            .also { tooltip += it }
    }

    override fun overrideOtherStackedOnMe(
        stack: ItemStack,
        other: ItemStack,
        slot: Slot,
        action: ClickAction,
        player: Player,
        access: SlotAccess,
    ): Boolean {
        if (action != SECONDARY) return false
        val consumable = stack.wishConsumable ?: return false
        return if (consumable.test(other)) {
            other.shrink(1)
            stack.wishRemain ++
            stack.wishLastConsumable = stack.wishRemain
            true
        } else false
    }

    fun consumable(
        stack: ItemStack, player: Player, shrink: Boolean = true
    ): Boolean {
        val consumable = stack.wishConsumable ?: return false
        val cs = player.inventory.firstOrNull { consumable.test(it) }
            ?: return false
        if (shrink) cs.shrink(1)
        return true
    }

    override fun getBarWidth(stack: ItemStack): Int
    = clamp(round(stack.wishRemain.toFloat()*13/stack.wishLastConsumable.toFloat()), 0, 13)
    override fun isBarVisible(stack: ItemStack): Boolean = true
    override fun getBarColor(itemStack: ItemStack): Int = Formatting.YELLOW.color!!

    class Result : Item(ItemSettings()
        .stacksTo(1)
        .component(RESULT_ITEMS_NBT.value(), mutableListOf())
    ), ItemUsingUtil {
        companion object {
            val regHelper = RegHelper
                .item { Result() }
            val RESULT_ITEMS_NBT: NbtTypeRegS<MutableList<ItemStack>>
                by nbtType(ItemStack.CODEC.listOf(), ItemStack.LIST_STREAM_CODEC)
            val RESULT_STAR_NBT: NbtTypeRegS<UByte> by nbtType(ModCodecs.UBYTE)
            var ItemStack.resultItems by RESULT_ITEMS_NBT.property(mutableListOf())
            var ItemStack.resultStar by RESULT_STAR_NBT.property(0u)
            fun getRows(size: Int): Int = (size-1)/9+1

            fun getRarity(stack: ItemStack): Rarity = when(stack.resultStar.toUInt()) {
                3u -> Rarity.RARE
                4u -> Rarity.EPIC
                5u -> Rarity.UNCOMMON
                else -> Rarity.COMMON
            }
        }
        fun getTran(star: Number): TranKey = tranKey().suffix("s$star")
        override fun getName(stack: ItemStack): Text
        = getTran(stack.resultStar.toByte()).text()
        override fun getUseDuration(itemStack: ItemStack, livingEntity: LivingEntity): Int = 60
        override fun getUseAnimation(itemStack: ItemStack): UseAnim = UseAnim.BOW
        override fun onUse(args: ItemUsingArgs): ItResultWith<ItemStack> = args.consumeUsingItem()
        override fun finishUsingItem(stack: ItemStack, level: Level, living: LivingEntity): ItemStack {
            if (living !is ServerPlayer) return stack
            living.openMenu(provider(stack))
            stack.shrink(1)
            return ItemStack.EMPTY
        }
        class Menu(
            syncId: Int, inv: Inventory, items: List<ItemStack>, val rows: Int = getRows(items.size)
        ) : ChestMenu(getChestMenuType(rows), syncId, inv, PickupOnlySimpleContainer(9*rows), rows) {
            init { items.forEachIndexed(container::set) }
            override fun removed(player: Player) {
                super.removed(player)
                container.removeAllItems().forEach {
                    if (!it.isEmpty) player.addItem(it) }
            }
            override fun getContainer(): SimpleContainer = super.getContainer() as SimpleContainer
            override fun quickMoveStack(player: Player, i: Int): ItemStack {
                val c = rows * 9
                if (i < c) return super.quickMoveStack(player, i)
                val stack = getSlot(i).item
                val copy = stack.copy()
                if (i in c until c+27 && !moveItemStackTo(stack, c+27, c+36, false))
                    return ItemStack.EMPTY
                if (i in c+27 until c+36 && !moveItemStackTo(stack, c, c+27, false))
                    return ItemStack.EMPTY
                return copy
            }
        }
        class PickupOnlySimpleContainer(i: Int) : SimpleContainer(i), ModContainer {
            override fun mayPlace(slot: Slot, stack: ItemStack): Boolean = false
        }
        fun provider(stack: ItemStack): MenuProvider = object : MenuProvider {
            override fun createMenu(i: Int, inventory: Inventory, player: Player)
            : Menu = Menu(i, inventory, stack.resultItems)
            override fun getDisplayName(): Text = this@Result.getName(stack) }
    }

    override fun canAttackBlock(s: BlockState, w: Level, p: BlockPos, l: Player): Boolean = false
    override fun getUseDuration(itemStack: ItemStack, livingEntity: LivingEntity): Int = 60
    override fun getUseAnimation(itemStack: ItemStack): UseAnim = UseAnim.BOW
    override fun onUse(args: ItemUsingArgs): ItResultWith<ItemStack> {
        val stack = args.stack
        if (stack.wishRemain > stack.wishLastConsumable)
            stack.wishLastConsumable = stack.wishRemain
        if (stack.wishRemain == 0u)
            return if (consumable(stack, args.player, shrink = false))
                args.consumeUsingItem() else args.fail()
        if (stack.wishGacha == null) return args.fail()
        if (!Regs.GACHA.containsKey(stack.wishGacha)) return args.fail()
        return args.consumeUsingItem()
    }
    override fun finishUsingItem(stack: ItemStack, level: Level, living: LivingEntity): ItemStack {
        if (!isServerThread()) return stack
        if (level !is ServerLevel) return stack
        if (living !is ServerPlayer) return stack
        val gacha = stack.wishGacha?.let { Regs.GACHA[it] } ?: return stack
        val remain = stack.wishRemain
        if (remain == 0u && !consumable(stack, living)) return stack
        else if (remain > 0u) stack.wishRemain --
        val data = stack.wishData.copy()
        val wish = Wish(gacha, data)
        val i = wish.nextList()
        val r: MutableList<ItemStack> = mutableListOf()
        i.item.forEach { r += it.getRandomItems(
            LootParams(level, mapOf(), mapOf(), living.luck)) }
        val ra = WishResultArgs(r, i.star, i.isUp, living, stack, gacha, data)
        Events.Custom.AFTER_WISH.invoker().invoke(ra)
        if (gacha is GachaWishListener) gacha.afterWish(ra)
        stack.wishData = data
        living.addItem(getResultStack(ra))
        return stack
    }
    fun getResultStack(ra: WishResultArgs): ItemStack {
        val result = ModItem.WISH_RESULT.value().default
        result.resultItems = ra.result
        result.resultStar = ra.star
        result.customModelData = CustomModelData(ra.star.toInt())
        result.rarityComponent = Result.getRarity(result)
        return result
    }

    data class WishResultArgs(
        var result: MutableList<ItemStack>,
        var star: UByte,
        var isUp: Boolean,

        val living: LivingEntity,
        val wish: ItemStack,
        val gacha: LootGacha,
        val data: WishData
    )

    interface GachaWishListener { fun afterWish(args: WishResultArgs) {} }
    interface GachaWishBreak : GachaWishListener {
        fun isBreak(args: WishResultArgs): Boolean = false
        override fun afterWish(args: WishResultArgs) { if (isBreak(args)) args.wish.shrink(1) }
    }

    interface IGachaOnce5 : GachaWishBreak { override fun isBreak(args: WishResultArgs): Boolean = args.star == 5u.toUByte() }
    interface IGachaOnceUp5 : GachaWishBreak { override fun isBreak(args: WishResultArgs): Boolean = args.star == 5u.toUByte() && args.isUp }

    open class GachaOnce5(parent: LootGacha? = null) : LootGacha(parent), IGachaOnce5
    open class GachaOnceUp5(parent: LootGacha? = null) : UpLootGacha(parent), IGachaOnceUp5

    object DefaultGacha {
        fun LootGacha.getId(): Id = Regs.GACHA.firstKey(this)
        infix fun Gacha<LootTable>.tranTo(value: String)
        { getId().toLanguageKey("gacha") tranTo value }
        fun lazyLootPool(f: (@LootUtil LootPoolBuilder).() -> Unit)
        : Lazy<List<LootTable>> = lazy { listOf( loot { pool(f) } ) }

        val DEFAULT = object : GachaOnce5() {
            override val pool3: List<LootTable> by lazyLootPool {
                item(ModItem.MORA, between(3, 13))
                item(ModItem.INTERTWINED_FATE, between(1, 4))
                item(Items.IRON_INGOT, between(3, 5))
                item(Items.GOLD_INGOT, between(4, 7))
                item(Items.DIAMOND, between(1, 4))
            }
            override val pool4: List<LootTable> by lazyLootPool {
                item(ModItem.PRIMOGEM, between(2, 7))
                item(ModItem.INTERTWINED_FATE, between(3, 7))
                item(Items.DIAMOND, between(7, 13))
                item(Items.ANCIENT_DEBRIS)
                item(Items.NETHERITE_SCRAP)
                item(Items.NETHER_STAR)
            }
            override val pool5: List<LootTable> by lazyLootPool {
                item(Items.NETHERITE_INGOT, between(4, 7))
                item(Items.NETHER_STAR, between(1, 5))
            }
        }.also { Regs.GACHA += "default".id to it }

        val PRESCIENCE_MATRIX = object : GachaOnceUp5(DEFAULT) {
            override val poolUp5: List<LootTable> by lazyLootPool { item(ModItem.PRESCIENCE_MATRIX) }
        }.also { Regs.GACHA += "prescience_matrix".id to it }
    }
}