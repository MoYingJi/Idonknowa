package moyingji.idonknowa.core

import dev.architectury.event.Event
import dev.architectury.event.EventFactory.createLoop
import dev.architectury.utils.Env
import dev.architectury.utils.Env.CLIENT
import moyingji.idonknowa.util.*

typealias EventAccept<T> = Event<(T) -> Unit>

object Events {
    @Retention(AnnotationRetention.BINARY)
    annotation class RunOnly(val value: Env)

    object ItemTooltip {
        @RunOnly(CLIENT) val BEFORE_ITEM: EventAccept<TooltipArgs> = createLoop()
        @RunOnly(CLIENT) val AFTER_ITEM: EventAccept<TooltipArgs> = createLoop()
    }

    fun default() {
        TooltipUtil.regSimpleAutoDesc()
        TooltipUtil.regAutoTooltipDataComp()
    }
}
