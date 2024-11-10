package moyingji.idonknowa.core

import dev.architectury.event.Event
import dev.architectury.event.events.client.ClientLifecycleEvent
import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.platform.Platform
import dev.architectury.utils.Env.*
import moyingji.idonknowa.Idonknowa
import moyingji.idonknowa.core.events.*
import moyingji.idonknowa.items.WishItem.WishResultArgs
import moyingji.idonknowa.util.*
import moyingji.lib.core.PropIn
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import dev.architectury.event.EventFactory.createLoop as create

typealias EventAccept<T> = Event<(T) -> Unit>

object Events {
    object ItemTooltip {
        @OnlyCallOn(CLIENT) val BEFORE_ITEM: EventAccept<TooltipArgs> = create()
        @OnlyCallOn(CLIENT) val AFTER_ITEM: EventAccept<TooltipArgs> = create()
    }
    object Entity {
        @OnlyCallOn(SERVER) val LIVING_HURT: Event<LivingEntity.(DamageSource, PropIn<Float>) -> Unit> = create()
    }
    object Player {
        val DROP_ITEM: EventAccept<PlayerDropItemModifier> = create()
        val STACK_INV_TICK: EventAccept<InventoryTickArgs> = create()
    }

    object Custom {
        @OnlyCallOn(SERVER) val AFTER_WISH: Event<(WishResultArgs) -> Unit> = create()
    }

    fun regEvents() {
        // 此处不能使用 Idonknowa 的 platform 判断, 因为此时 Idonknowa 还未初始化
        if (Platform.getEnvironment() == CLIENT)
            ClientLifecycleEvent.CLIENT_SETUP.register {
                Idonknowa.initClient(it) }
        LifecycleEvent.SERVER_BEFORE_START.register {
            Idonknowa.threadServer = Thread.currentThread()
            Idonknowa.currentServer = it }
        LifecycleEvent.SERVER_STOPPED.register {
            Idonknowa.threadServer = null
            Idonknowa.currentServer = null }
    }
}
