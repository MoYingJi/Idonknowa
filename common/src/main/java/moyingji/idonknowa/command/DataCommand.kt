package moyingji.idonknowa.command

import com.google.gson.JsonElement
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.JsonOps
import moyingji.idonknowa.Idonknowa.id
import moyingji.idonknowa.Text
import moyingji.idonknowa.command.argument.JsonPathArgument
import moyingji.idonknowa.command.argument.JsonPathArgument.*
import moyingji.idonknowa.lang.text
import moyingji.lib.util.typed
import net.minecraft.world.item.ItemStack

object DataCommand {
    val data: LiteralNode by literal {
        then(getHand)
    }

    val playerHandJsonArg = JsonPathArgument(
        ::getDataInHand.also { JsonPathArgument.fr += "player_hand_stack".id to it },
        defaultFirst = "components",
        autoPrefix = listOf("minecraft:", "idonknowa:")
    ).also { JsonPathArgument.type }
    val getHand: LiteralNode by literal {
        run { success(getData(getDataInHand(this), null)) }
        argument("path", playerHandJsonArg) {
            run { success(getData(getDataInHand(this),
                JsonPathArgument.getResult(this, "path"))) }
        }
    }
    fun getDataInHand(c: CommandContext<*>): JsonElement
    = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, c
        .typed<CmdContext>().player.mainHandItem)
        .result().get()
    fun getData(json: JsonElement, result: JsonPathResult?)
    : Text = result?.getParentNameElement(json)?.third?.toString()?.text() ?: json.toString().text()
}