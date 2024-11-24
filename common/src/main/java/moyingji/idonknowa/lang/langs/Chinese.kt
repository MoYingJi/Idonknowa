package moyingji.idonknowa.lang.langs

import moyingji.idonknowa.advancement.*
import moyingji.idonknowa.all.*
import moyingji.idonknowa.datagen.LangProvider
import moyingji.idonknowa.items.*
import moyingji.idonknowa.items.WishItem.DefaultGacha.tranTo
import moyingji.idonknowa.lang.*
import moyingji.idonknowa.util.idonknowaDescSufShift
import moyingji.lib.util.typed

object Chinese : LangProvider {
    override val languageCode: String = "zh_cn"
    override fun genTranslations() {
        Translations.MOD_MENU_NAME to "Idonknowa"

        // region Items
        ModItem.PRIMOGEM tranTo "原石"
        ModItem.MORA tranTo "摩拉"
        ModItem.INTERTWINED_FATE tranTo "纠缠之缘"
        // endregion

        // region Blocks
        ModBlock.PRIMOGEM_ORE tranTo "深层原石矿石"
        // endregion

        // region Classes
        ModItem.TEST_ITEM.value().typed<_TestItem>().apply {
            this tranTo "Idonknowa 仙女棒"
            refineName tranTo "来测"
            refineDesc tranLines """
                一个平平无奇的测试物品罢了 甚至精@{level}
                听说作者常会堆一些未发布特性在这
                拿在副手上可以显示一些事件的详细数据哦
            """.trimIndent()
        }
        ModItem.REFINE_TEMP.value().typed<ModSmithingTemplate>().builder.apply {
            key tranTo "精炼模板"
            appliesTo tranTo "可精炼物品"
            ingredients tranTo "需求物品"
            title tranTo "精炼提升锻造模板"
            baseDesc tranTo "需要精炼的物品"
            additionsDesc tranTo "可添加的附加物品 一般是物品本身"
        }
        ModItem.WISH_ITEM.value().typed<WishItem>().apply {
            this tranTo "祈愿"
            GACHA_KEY tranTo "卡池"
            CONSUMABLE_KEY tranTo "剩余祈愿次数"
            idonknowaDescSufShift() tranLines """
                长按右键使用可以进行祈愿
                有祈愿次数或背包中有相应物品时才能祈愿
                拿起祈愿所需物品对此物品右键可以补充祈愿次数
            """.trimIndent()
            with(ModItem.WISH_RESULT.value().typed<WishItem.Result>()) {
                getTran(3) tranTo "单抽无奇迹"
                getTran(4) tranTo "出紫"
                getTran(5) tranTo "出金"
            }
            also {
                WishItem.DefaultGacha.DEFAULT tranTo "默认卡池"
                WishItem.DefaultGacha.PRESCIENCE_MATRIX tranTo "穷观妙算"
            }
        }
        ModItem.PRESCIENCE_MATRIX.value().typed<PrescienceMatrix>().apply {
            this tranTo "穷观阵"
            refineName tranTo "太微行棋，灵台示影"
            refineDesc tranLines """
                右击开启【穷观阵】 无法同时开启多个和叠加效果
                使所承受的未受护盾抵挡之前的伤害的 @{distributed}% 分摊给物品
                （单独计算 不与抗性提升等叠加）
                每 1 点生命值消耗 120 点耐久
                开启时 每 @{damageTick}tick 消耗 1 点耐久
                最大耐久度为 @{maxDamage} (耐久消耗可被耐久附魔减少)
                耐久耗尽时不会损坏 但会自动关闭
            """.trimIndent()
        }
        // endregion

        // region Advancement
        ModAdvancement.root.apply {
            titleKey tranTo "Idonknowa"
            descriptionKey tranTo "一个无用的模组"
        }
        // endregion

        // region Others
        ModTab.tranKey tranTo "Idonknowa"

        Translations.ERROR tranTo "发生了错误"

        Translations.REFINE_LEVEL tranTo "精炼 @{level} 阶"

        Translations.PRESS_TO tranTo "按住 [@{key}] 以@{do}"

        Translations.DISPLAY_DETAILS tranTo "显示详细信息"

        Translations.OWNER tranTo "拥有者"

        Translations.CURRENT_STATE tranTo "当前状态"
        Translations.STATE_ON tranTo "开启"
        Translations.STATE_OFF tranTo "关闭"
        // endregion
    }
}