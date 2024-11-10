package moyingji.idonknowa.command

import dev.architectury.event.events.common.CommandRegistrationEvent

object ModCommand {
    val commands: MutableList<RegCmdNode> = mutableListOf()
    init { CommandRegistrationEvent.EVENT.register {
        dispatcher, reg, env ->
        for (r in commands) r.node = dispatcher.register(r.applied) } }

    val idonknowa by regLiteral {
        then(DataCommand.data)
    }

    init {
        DataCommand // init
    }
}