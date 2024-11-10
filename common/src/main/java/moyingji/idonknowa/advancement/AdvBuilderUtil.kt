package moyingji.idonknowa.advancement

import moyingji.idonknowa.*
import moyingji.idonknowa.Idonknowa.id
import moyingji.idonknowa.Idonknowa.isDatagen
import moyingji.idonknowa.core.RegS
import moyingji.idonknowa.lang.*
import moyingji.idonknowa.util.*
import moyingji.lib.api.autoName
import moyingji.lib.core.*
import moyingji.lib.util.toOptional
import net.minecraft.advancements.*
import net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance.hasItems
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KProperty

typealias AdvHolder = AdvancementHolder
typealias CritMap = MutableMap<String, Criterion<*>>

class AdvBuilder(
    var parent: AdvHolder? = null,
    val namespace: String = Idonknowa.MOD_ID,
    f: AdvBuilder.() -> Unit
) : PropertyDelegateProvider<Any?, PropRead<AdvHolder>> {
    val builder: Advancement.Builder = Advancement.Builder.advancement()

    fun parent(parent: AdvHolder?): AdvBuilder = also { this.parent = parent }

    // region Display Info
    var icon: (() -> ItemStack)? = null; infix fun icon(icon: () -> ItemStack): AdvBuilder = also { this.icon = icon }
    var title: Text? = null; infix fun title(title: Text): AdvBuilder = also { this.title = title }
    var description: Text? = null; infix fun description(description: Text): AdvBuilder = also { this.description = description }
    var background: Id? = null; infix fun background(background: Id?): AdvBuilder = also { this.background = background }
    var type: AdvancementType = AdvancementType.TASK; infix fun type(type: AdvancementType): AdvBuilder = also { this.type = type }
    var showToast: Boolean = true; fun showToast(showToast: Boolean = true): AdvBuilder = also { this.showToast = showToast }
    var announceChat: Boolean = true; fun announceChat(announceChat: Boolean = true): AdvBuilder = also { this.announceChat = announceChat }
    var hidden: Boolean = false; fun hidden(hidden: Boolean = false): AdvBuilder = also { this.hidden = hidden }

    var titleKey: TranslationKey? = null
    var descriptionKey: TranslationKey? = null
    infix fun title(key: TranslationKey): AdvBuilder = title(key.text()).also { titleKey = key }
    infix fun description(key: TranslationKey): AdvBuilder = description(key.text()).also { descriptionKey = key }
    infix fun background(path: String): AdvBuilder = background(path.id(namespace))
    fun noToast(): AdvBuilder = showToast(false)
    fun noAnnounce(): AdvBuilder = announceChat(false)

    infix fun icon(stack: ItemStack): AdvBuilder = icon { stack }
    infix fun icon(item: ItemLike): AdvBuilder = icon { item.default }
    infix fun icon(regs: RegS<out ItemLike>): AdvBuilder = icon { regs.value().default }

    var displayInfo: DisplayInfo? = null
    private fun displayInfo(): DisplayInfo = displayInfo ?: DisplayInfo(
        icon!!(), title!!, description!!, background.toOptional(),
        type, showToast, announceChat, hidden
    ).also { displayInfo = it }
    // endregion

    /** 保证 [title] 和 [description] 都有值 */
    @Suppress("DuplicatedCode") fun auto(id: Id) {
        if (titleKey == null)
            if (title != null) {
                val title = title as? MutableText
                val content = title?.contents as? TranslatableContents
                if (content != null) titleKey = content.key.tranKey()
            } else titleKey = id.toLanguageKey("adv", "title").tranKey()
        val titleKey = titleKey
        if (title == null && titleKey != null)
            title = titleKey.text()
        if (descriptionKey == null)
            if (description != null) {
                val description = description as? MutableText
                val content = description?.contents as? TranslatableContents
                if (content != null) descriptionKey = content.key.tranKey()
            } else descriptionKey = id.toLanguageKey("adv", "desc").tranKey()
        val descriptionKey = descriptionKey
        if (description == null && descriptionKey != null)
            description = descriptionKey.text()
    }

    val criterion: CritMap = mutableMapOf()
    fun criterion(name: String, criterion: Criterion<*>): AdvBuilder = also { this.criterion[name] = criterion }
    infix fun String.crit(criterion: Criterion<*>): AdvBuilder = criterion(this, criterion)
    infix fun CritMap.got(item: ItemLike): CritMap = also {
        this += "got_${item.asItem().idOrThrow().path}" to hasItems(item)
        if (icon == null) icon(item) }
    infix fun CritMap.got(item: RegS<out ItemLike>): CritMap = also {
        this += "got_${item.id.path}" to hasItems(item.value())
        if (icon == null) icon(item) }

    fun regBuild(id: Id): AdvHolder {
        auto(id)
        parent?.let { builder.parent(it) }
        builder.display(displayInfo())
        criterion.forEach(builder::addCriterion)
        val holder = builder.build(id)
        if (isDatagen) ModAdvancement.advancements += holder
        adv += holder to this
        return holder
    }
    override fun provideDelegate(thisRef: Any?, property: KProperty<*>): PropRead<AdvHolder>
    = PropProvider(regBuild(property.autoName(String::lowercase).id(namespace)))

    init { f(this) }
}

val adv: MutableMap<AdvHolder, AdvBuilder> = mutableMapOf()

val AdvHolder.titleKey: Translatable get() = adv[this]?.titleKey ?: Translatable.EMPTY
val AdvHolder.descriptionKey: Translatable get() = adv[this]?.descriptionKey ?: Translatable.EMPTY