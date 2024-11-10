package moyingji.idonknowa.command.argument

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.*
import moyingji.idonknowa.*
import moyingji.idonknowa.Idonknowa.id
import moyingji.idonknowa.command.*
import moyingji.idonknowa.command.argument.JsonPathArgument.JsonPathResult
import moyingji.idonknowa.core.RegS
import moyingji.idonknowa.util.*
import moyingji.lib.util.*
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.arguments.NbtPathArgument.ERROR_INVALID_NODE
import net.minecraft.commands.synchronization.ArgumentTypeInfo
import net.minecraft.network.FriendlyByteBuf
import java.util.concurrent.CompletableFuture
import kotlin.collections.listOf

class JsonPathArgument(
    val template: Template
) : ArgumentType<JsonPathResult>, SuggestWithArgType {
    constructor(
        f: CommandContext<*>.() -> JsonElement,
        separator: Char = '.',
        defaultFirst: String = "",
        autoPrefix: List<String> = listOf()
    ) : this(Template(f, separator, defaultFirst, autoPrefix))

    @Throws(CommandSyntaxException::class)
    override fun parse(reader: StringReader): JsonPathResult {
        val path = if (reader.remaining.contains(' '))
            reader.readStringUntil(' ')
        else reader.remaining.also { reader.cursor = reader.totalLength }
        return JsonPathResult(template, path.split(template.separator))
    }
    override fun listSuggestions(
        input: String,
        context: CommandContext<*>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> = supplyAsync {
        val sp = template.separator.toString()
        val path = input.split(sp)
            .let { it.subList(0, it.size - 1) }
        val e = runCatching { findElement(
            path, template.f(context), template.autoPrefix, template.defaultFirst) }
            .getOrElse { return@supplyAsync builder.build() }
        getKeys(e.second).forEach {
            var a = it
            for (p in template.autoPrefix)
                if (a.startsWith(p)) { a = a.substringAfter(p); break }
            var i = input.trim()
            val ind = i.lastIndexOf(sp)
            i = when (ind) {
                -1 -> ""; 0 -> sp
                else -> i.substring(0, ind + 1)
            }
            builder.suggest("$i$a")
        }
        return@supplyAsync builder.build()
    }

    data class JsonPathResult(
        val template: Template,
        val path: List<String>
    ) {
        fun getParentNameElement(element: JsonElement)
        : Triple<JsonElement?, String?, JsonElement> {
            lastKey = null
            val (p, e) = findElement(
                path, element, template.autoPrefix, template.defaultFirst)
            return Triple(p, lastKey.also { lastKey = null }, e)
        }
    }

    companion object {
        @Throws(CommandSyntaxException::class)
        tailrec fun findElement(
            path: List<String>,
            element: JsonElement,
            autoPrefix: List<String> = listOf(),
            replaceEmpty: String? = null,
            keepReplace: Boolean = false,
        ): Pair<JsonElement?, JsonElement> {
            if (path.isEmpty()) return null to element
            val t = path[0]
            var e = getElement(element, t)
            var nextParent: JsonElement? = element
            if (e == null && t.isBlank() && !replaceEmpty.isNullOrBlank())
                e = getElement(element, replaceEmpty) ?: throw ERROR_INVALID_NODE.create()
            if (e == null && !replaceEmpty.isNullOrBlank())
                e = getElement(getElement(element, replaceEmpty).also { nextParent = it }, t)
            if (e == null) for (p in autoPrefix) {
                e = getElement(element, "$p$t")
                if (e != null) break }
            if (e == null) throw ERROR_INVALID_NODE.create()
            val replaceNext = if (keepReplace) replaceEmpty else null
            return if (path.size == 1 || (path.size == 2 && path[1].isBlank())) nextParent to e
            else findElement(path.subList(1, path.size), e, autoPrefix, replaceNext, keepReplace)
        }
        fun getElement(element: JsonElement?, path: String): JsonElement? = when (true) {
            (element == null) -> null
            element.isJsonObject -> element.asJsonObject[path]
            element.isJsonArray -> element.asJsonArray[path.toInt()]
            else -> null
        }.also { if (it != null) lastKey = path }; var lastKey: String? = null

        fun getKeys(element: JsonElement?): List<String> = when (true) {
            (element == null) -> listOf()
            element.isJsonObject -> element.asJsonObject.keySet().toList()
            element.isJsonArray -> (0 until element.asJsonArray.size()).map { it.toString() }
            else -> listOf()
        }

        val fr: MutableMap<Id, CommandContext<*>.() -> JsonElement> = mutableMapOf()

        val type: RegS<Info> = regArgType("json_path".id, JsonPathArgument::class) { Info() }
        fun getResult(context: CommandContext<*>, name: String): JsonPathResult
        = context.getArgument(name, JsonPathResult::class.java)
    }
    class Info : ArgumentTypeInfo<JsonPathArgument, Template> {
        override fun serializeToNetwork(template: Template, buffer: FriendlyByteBuf) {
            buffer.writeResourceLocation(fr.firstKey(template.f))
            buffer.writeChar(template.separator.code)
            buffer.writeUtf(template.defaultFirst)
            buffer.writeCollection(template.autoPrefix, FriendlyByteBuf::writeUtf)
        }
        override fun deserializeFromNetwork(buffer: FriendlyByteBuf): Template = Template(
            buffer.readResourceLocation().let { fr[it]!! },
            buffer.readChar(),
            buffer.readUtf(),
            buffer.readList(FriendlyByteBuf::readUtf)
        )
        override fun serializeToJson(template: Template, json: JsonObject) {
            json.addProperty("f", fr.firstKey(template.f).toString())
            json.addProperty("separator", template.separator)
            json.addProperty("default_first", template.defaultFirst)
            json.add("auto_prefix", JsonArray().also { template.autoPrefix.forEach(it::add) })
        }
        override fun unpack(argument: JsonPathArgument): Template = argument.template
    }
    data class Template(
        val f: CommandContext<*>.() -> JsonElement,
        val separator: Char = '.',
        val defaultFirst: String = "",
        val autoPrefix: List<String> = listOf()
    ) : ArgumentTypeInfo.Template<JsonPathArgument> {
        override fun type(): ArgumentTypeInfo<JsonPathArgument, *> = type.value()
        override fun instantiate(context: CommandBuildContext)
        : JsonPathArgument = JsonPathArgument(this)
    }
}