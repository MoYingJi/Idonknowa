package moyingji.idonknowa.core

import dev.architectury.registry.registries.*
import moyingji.idonknowa.*
import moyingji.idonknowa.Idonknowa.id
import moyingji.idonknowa.all.ItemSettings
import moyingji.idonknowa.datagen.ModModelProvider
import moyingji.lib.api.autoName
import moyingji.lib.core.*
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.*
import net.minecraft.world.level.block.Block
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KProperty

typealias RegS<T> = RegistrySupplier<T>

open class RegHelper<T>(
    registry: ResourceKey<Registry<T>>?,
    private val initializer: () -> T
) : PropertyDelegateProvider<Any?, PropRead<RegS<T>>> {
    protected open val reg: Registrar<T> = manager.get(registry)
    lateinit var regs : RegS<T>
    open fun reg(id: Id): RegS<T> = reg.register(id, initializer).also { regs = it }
        .also { listeners.removeAll { regs.listen(it); true } }
        .also { regs.listen { ModModelProvider.whenReg(it) } }

    override fun provideDelegate(thisRef: Any?, property: KProperty<*>)
    : PropRead<RegS<T>> = reg(provideId(property)).propProvider()
        .also { listen { afterReg(it, property) } }
    open fun provideId(prop: KProperty<*>): Id
    = prop.autoName("_", String::lowercase).id

    private val listeners: MutableList<(T) -> Unit> = mutableListOf()
    open fun listen(listener: (T) -> Unit): RegHelper<T> = this.also {
        if (::regs.isInitialized) regs.listen(listener)
        else listeners += listener
    }

    open fun afterReg(obj: T, prop: KProperty<*>) {}

    companion object {
        val manager: RegistrarManager = RegistrarManager.get(Idonknowa.MOD_ID)

        fun item(init: () -> Item) = ItemRegHelper(init)
        fun block(init: () -> Block) = BlockRegHelper(init)

        fun itemSettings(init: ItemSettings.() -> Unit = {})
        = item { Item(ItemSettings().also(init)) }
    }
}


class ItemRegHelper(initializer: () -> Item) : RegHelper<Item>(Registries.ITEM, initializer) {
    override fun listen(listener: (Item) -> Unit): ItemRegHelper = this.also { super.listen(listener) }
}

class BlockRegHelper(initializer: () -> Block) : RegHelper<Block>(Registries.BLOCK, initializer) {
    override fun listen(listener: (Block) -> Unit): BlockRegHelper = this.also { super.listen(listener) }
    fun blockItem(
        settings: ItemSettings = ItemSettings()
    ): BlockRegHelper = listen { item { BlockItem(regs.get(), settings) }.reg(regs.id) }
    fun blockItem(
        settings: ItemSettings.() -> ItemSettings
    ): BlockRegHelper = blockItem(settings(ItemSettings()))
}