@file:Suppress("UnusedReceiverParameter")
package moyingji.idonknowa.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.*
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.*
import com.mojang.brigadier.suggestion.*
import com.mojang.brigadier.tree.LiteralCommandNode
import dev.architectury.registry.registries.Registrar
import moyingji.idonknowa.*
import moyingji.idonknowa.command.CommandUtil.contextReturn
import moyingji.idonknowa.command.argument.*
import moyingji.idonknowa.core.*
import moyingji.idonknowa.lang.text
import moyingji.idonknowa.mixin.ResourceKeyArgumentAccessor
import moyingji.idonknowa.mixin.command.ArgumentTypeInfosAccessor
import moyingji.lib.api.autoName
import moyingji.lib.core.PropRead
import moyingji.lib.math.*
import moyingji.lib.util.supplyAsync
import net.minecraft.commands.*
import net.minecraft.commands.arguments.*
import net.minecraft.commands.arguments.coordinates.*
import net.minecraft.commands.synchronization.ArgumentTypeInfo
import net.minecraft.core.*
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import org.joml.Vector3d
import java.util.concurrent.CompletableFuture
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.*

@DslMarker @Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.TYPE) annotation class CmdDsl

typealias ServerSource = CommandSourceStack
typealias LiteralNode = LiteralCommandNode<ServerSource>
typealias CmdContext = @CmdDsl CommandContext<ServerSource>

typealias ArgBuilder = ArgumentBuilder<ServerSource, *>
typealias LiteralBuilder = LiteralArgumentBuilder<ServerSource>
typealias RequiredBuilder = RequiredArgumentBuilder<ServerSource, *>

typealias LiteralSetter = (@CmdDsl LiteralBuilder).() -> Unit
typealias RequiredSetter = (@CmdDsl RequiredBuilder).() -> Unit
typealias CmdExceptionProv = BuiltInExceptionProvider.() -> CommandSyntaxException

class RegCmdNode(
    val builder: LiteralBuilder,
    val apply: LiteralSetter = {}
) : PropRead<LiteralNode> {
    lateinit var node: LiteralNode
    val applied: LiteralBuilder by lazy { builder.apply(apply) }
    override val value: LiteralNode get() {
        return if (::node.isInitialized) node
        else if (!ModCommand.commands.contains(this))
            applied.build().also { node = it }
        else throw IllegalStateException("Root command not registered!")
    }
}
class RegCmdNodeWithoutName(
    val builder: (name: String) -> RegCmdNode,
) : PropertyDelegateProvider<Any?, RegCmdNode> {
    override fun provideDelegate(thisRef: Any?, property: KProperty<*>)
    : RegCmdNode = builder(property.autoName)
}
object CommandUtil {
    val contextReturn: MutableMap<CmdContext, Int> = mutableMapOf()
}

fun regLiteral(name: String, block: LiteralSetter): RegCmdNode = literal(name, block).also { ModCommand.commands += it }
fun regLiteral(block: LiteralSetter): RegCmdNodeWithoutName = RegCmdNodeWithoutName { regLiteral(it, block) }
fun literal(name: String, block: LiteralSetter): RegCmdNode = RegCmdNode(Commands.literal(name), block)
fun literal(block: LiteralSetter): RegCmdNodeWithoutName = RegCmdNodeWithoutName { literal(it, block) }

fun ArgBuilder.requiresOp() { requires { it.hasPermission(2) } }
fun ArgBuilder.runCode(f: (CmdContext).() -> Int) { executes(f) }
fun ArgBuilder.run(default: Int = 0, f: (CmdContext).() -> Unit)
{ executes { contextReturn.remove(it); f(it); contextReturn.remove(it) ?: default } }
infix fun CmdContext.code(i: Int) { contextReturn[this] = i }

fun CmdContext.succeed() { code(Command.SINGLE_SUCCESS) }
fun CmdContext.fail(code: Int = -1) { if (code != Command.SINGLE_SUCCESS) code(code) else throw IllegalArgumentException() }
inline fun CmdContext.fail(f: CmdExceptionProv): Nothing = throw f(CommandSyntaxException.BUILT_IN_EXCEPTIONS)
fun CmdContext.success(text: Text, log: Boolean = true) { source.sendSuccess({text}, log); succeed() }
fun CmdContext.success(text: String, log: Boolean = true) { success(text.text(), log) }
fun CmdContext.failure(text: Text) { source.sendFailure(text); fail() }
fun CmdContext.failure(text: String) { failure(text.text()) }
fun CmdContext.message(text: Text) { source.sendSystemMessage(text) }
fun CmdContext.message(text: String) { message(text.text()) }

inline fun <T> CmdContext.runCatchingFail(
    exception: CmdExceptionProv = { dispatcherUnknownCommand().create() },
    block: CmdContext.() -> T
): Result<T> = runCatching(block).onFailure { fail(exception) }

val CmdContext.player: ServerPlayer get() = source.playerOrException

fun suggestion(f: (@CmdDsl SuggestionsBuilder).(CmdContext) -> Unit): SuggestionProvider<ServerSource>
= SuggestionProvider { c, b -> Unit.supplyAsync { f(b, c); b.build() } }
fun RequiredBuilder.suggestion(f: (@CmdDsl SuggestionsBuilder).(CmdContext) -> Unit)
{ suggests { c, b -> supplyAsync { f(b, c); b.build() } } }

fun ArgBuilder.literal(name: String, block: LiteralSetter) { then(Commands.literal(name).apply(block)) }
fun ArgBuilder.argument(name: String, type: ArgumentType<*>, block: RequiredSetter) { then(Commands.argument(name, type).apply(block)) }
fun ArgBuilder.argument(arg: CmdArgument, block: RequiredSetter) { argument(arg.name, arg.type) { suggests(arg.suggestion); block() } }

fun ArgBuilder.arguments(vararg args: CmdArgument, block: RequiredSetter) {
    require(args.isNotEmpty())
    var builder: ArgBuilder = this
    val m: MutableList<Pair<ArgBuilder, RequiredBuilder>> = mutableListOf()
    for ((name, type, suggestion) in args) {
        val b: RequiredBuilder = Commands.argument(name, type)
        suggestion?.let { b.suggests(it) }
        m += builder to b
        builder = b
    }
    if (builder is RequiredBuilder) block.invoke(builder)
    m.reversed().forEach { (b, r) -> b.then(r) }
}
infix fun String.arg(type: ArgumentType<*>): CmdArgument = CmdArgument(this, type)
infix fun CmdArgument.suggest(suggestion: SuggestionProvider<ServerSource>): CmdArgument = this.also { it.suggestion = suggestion }
infix fun CmdArgument.suggest(suggestion: SuggestionsBuilder.(CommandContext<ServerSource>) -> Unit): CmdArgument
= this.also { it.suggestion = SuggestionProvider { c, b ->
    suggestion(b, c); b.buildFuture() } }
infix fun CmdArgument.suggestAsync(
    suggestion: suspend SuggestionsBuilder.(CommandContext<ServerSource>) -> Unit
): CmdArgument
= this.also { it.suggestion = SuggestionProvider { c, b ->
    supplyAsync { suggestion(b, c); b.build() } } }

data class CmdArgument(
    var name: String,
    var type: ArgumentType<*>,
    var suggestion: SuggestionProvider<ServerSource>? = null
)

interface SuggestWithArgType {
    fun listSuggestions(
        input: String,
        context: CommandContext<*>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions>
}

// region Argument Types

// region Number Arguments | 此区域 AI 生成 非必要勿动 展开一大坨
// 参数可以是本来的 Number 或者 Int, Double 这种通用参数
// 表示范围也可以使用 ClosedRange

fun byteInt(min: Byte = Byte.MIN_VALUE, max: Byte = Byte.MAX_VALUE): ArgumentType<Int> = IntegerArgumentType.integer(min.toInt(), max.toInt())
fun byteInt(): ArgumentType<Int> = byteInt(min = Byte.MIN_VALUE, max = Byte.MAX_VALUE)
fun byteInt(min: Int = Byte.MIN_VALUE.toInt(), max: Int = Byte.MAX_VALUE.toInt()): ArgumentType<Int> = IntegerArgumentType.integer(min, max).also { require(min >= Byte.MIN_VALUE && max <= Byte.MAX_VALUE) }
fun byteInt(range: ClosedRange<Byte>): ArgumentType<Int> = byteInt(range.s, range.e)
fun byteInt(range: IntRange): ArgumentType<Int> = byteInt(range.s, range.e)
fun ArgBuilder.argByte(name: String, min: Byte = Byte.MIN_VALUE, max: Byte = Byte.MAX_VALUE, block: RequiredSetter) { argument(name, byteInt(min, max), block) }
fun ArgBuilder.argByte(name: String, min: Int = Byte.MIN_VALUE.toInt(), max: Int = Byte.MAX_VALUE.toInt(), block: RequiredSetter) { argument(name, byteInt(min, max), block) }
fun ArgBuilder.argByte(name: String, range: ClosedRange<Byte>, block: RequiredSetter) { argument(name, byteInt(range), block) }
fun ArgBuilder.argByte(name: String, range: IntRange, block: RequiredSetter) { argument(name, byteInt(range), block) }
fun ArgBuilder.argByte(name: String, block: RequiredSetter) { argument(name, byteInt(), block) }
fun CmdContext.argByte(name: String): Byte = IntegerArgumentType.getInteger(this, name).toByte()

fun shortInt(min: Short = Short.MIN_VALUE, max: Short = Short.MAX_VALUE): ArgumentType<Int> = IntegerArgumentType.integer(min.toInt(), max.toInt())
fun shortInt(): ArgumentType<Int> = shortInt(min = Short.MIN_VALUE, max = Short.MAX_VALUE)
fun shortInt(min: Int = Short.MIN_VALUE.toInt(), max: Int = Short.MAX_VALUE.toInt()): ArgumentType<Int> = IntegerArgumentType.integer(min, max).also { require(min >= Short.MIN_VALUE && max <= Short.MAX_VALUE) }
fun shortInt(range: ClosedRange<Short>): ArgumentType<Int> = shortInt(range.s, range.e)
fun shortInt(range: IntRange): ArgumentType<Int> = IntegerArgumentType.integer(range.s, range.e).also { require(range.s >= Short.MIN_VALUE && range.e <= Short.MAX_VALUE) }
fun ArgBuilder.argShort(name: String, min: Short = Short.MIN_VALUE, max: Short = Short.MAX_VALUE, block: RequiredSetter) { argument(name, shortInt(min, max), block) }
fun ArgBuilder.argShort(name: String, min: Int = Short.MIN_VALUE.toInt(), max: Int = Short.MAX_VALUE.toInt(), block: RequiredSetter) { argument(name, shortInt(min, max), block) }
fun ArgBuilder.argShort(name: String, range: ClosedRange<Short>, block: RequiredSetter) { argument(name, shortInt(range), block) }
fun ArgBuilder.argShort(name: String, range: IntRange, block: RequiredSetter) { argument(name, shortInt(range), block) }
fun ArgBuilder.argShort(name: String, block: RequiredSetter) { argument(name, shortInt(), block) }
fun CmdContext.argShort(name: String): Short = IntegerArgumentType.getInteger(this, name).toShort()

fun integer(min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE): ArgumentType<Int> = IntegerArgumentType.integer(min, max)
fun integer(range: ClosedRange<Int>): ArgumentType<Int> = integer(range.s, range.e)
fun ArgBuilder.argInt(name: String, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE, block: RequiredSetter) { argument(name, integer(min, max), block) }
fun ArgBuilder.argInt(name: String, range: ClosedRange<Int>, block: RequiredSetter) { argument(name, integer(range), block) }
fun ArgBuilder.argInt(name: String, block: RequiredSetter) { argument(name, integer(), block) }
fun CmdContext.argInt(name: String): Int = IntegerArgumentType.getInteger(this, name)

fun long(min: Long = Long.MIN_VALUE, max: Long = Long.MAX_VALUE): ArgumentType<Long> = LongArgumentType.longArg(min, max)
fun long(range: ClosedRange<Long>): ArgumentType<Long> = LongArgumentType.longArg(range.s, range.e)
fun long(range: IntRange): ArgumentType<Long> = LongArgumentType.longArg(range.s.toLong(), range.e.toLong())
fun ArgBuilder.argLong(name: String, min: Long = Long.MIN_VALUE, max: Long = Long.MAX_VALUE, block: RequiredSetter) { argument(name, long(min, max), block) }
fun ArgBuilder.argLong(name: String, range: ClosedRange<Long>, block: RequiredSetter) { argument(name, long(range), block) }
fun ArgBuilder.argLong(name: String, range: IntRange, block: RequiredSetter) { argument(name, long(range), block) }
fun ArgBuilder.argLong(name: String, block: RequiredSetter) { argument(name, long(), block) }
fun CmdContext.argLong(name: String): Long = LongArgumentType.getLong(this, name)

fun float(min: Float = Float.MIN_VALUE, max: Float = Float.MAX_VALUE): ArgumentType<Float> = FloatArgumentType.floatArg(min, max)
fun float(): ArgumentType<Float> = float(min = Float.MIN_VALUE, max = Float.MAX_VALUE)
fun float(min: Number = Float.MIN_VALUE, max: Number = Float.MAX_VALUE): ArgumentType<Float> = float(min.toFloat(), max.toFloat())
fun float(range: ClosedRange<Float>): ArgumentType<Float> = FloatArgumentType.floatArg(range.s, range.e)
fun float(range: ClosedFloatingPointRange<Double>): ArgumentType<Float> = FloatArgumentType.floatArg(range.s.toFloat(), range.e.toFloat())
fun ArgBuilder.argFloat(name: String, min: Float = Float.MIN_VALUE, max: Float = Float.MAX_VALUE, block: RequiredSetter) { argument(name, float(min, max), block) }
fun ArgBuilder.argFloat(name: String, min: Number = Float.MIN_VALUE, max: Number = Float.MAX_VALUE, block: RequiredSetter) { argument(name, float(min, max), block) }
fun ArgBuilder.argFloat(name: String, range: ClosedRange<Float>, block: RequiredSetter) { argument(name, float(range), block) }
fun ArgBuilder.argFloat(name: String, range: ClosedFloatingPointRange<Double>, block: RequiredSetter) { argument(name, float(range), block) }
fun ArgBuilder.argFloat(name: String, block: RequiredSetter) { argument(name, float(), block) }
fun CmdContext.argFloat(name: String): Float = FloatArgumentType.getFloat(this, name)

fun double(min: Double = Double.MIN_VALUE, max: Double = Double.MAX_VALUE): ArgumentType<Double> = DoubleArgumentType.doubleArg(min, max)
fun double(): ArgumentType<Double> = double(min = Double.MIN_VALUE, max = Double.MAX_VALUE)
fun double(min: Number = Double.MIN_VALUE, max: Number = Double.MAX_VALUE): ArgumentType<Double> = double(min.toDouble(), max.toDouble())
fun double(range: ClosedRange<Double>): ArgumentType<Double> = DoubleArgumentType.doubleArg(range.s, range.e)
fun ArgBuilder.argDouble(name: String, min: Double = Double.MIN_VALUE, max: Double = Double.MAX_VALUE, block: RequiredSetter) { argument(name, double(min, max), block) }
fun ArgBuilder.argDouble(name: String, min: Number = Double.MIN_VALUE, max: Number = Double.MAX_VALUE, block: RequiredSetter) { argument(name, double(min, max), block) }
fun ArgBuilder.argDouble(name: String, range: ClosedRange<Double>, block: RequiredSetter) { argument(name, double(range), block) }
fun ArgBuilder.argDouble(name: String, block: RequiredSetter) { argument(name, double(), block) }
fun CmdContext.argDouble(name: String): Double = DoubleArgumentType.getDouble(this, name)

// endregion
// region BooleanArgument
fun boolean(): ArgumentType<Boolean> = BoolArgumentType.bool()
fun ArgBuilder.argBool(name: String, block: RequiredSetter) { argument(name, boolean(), block) }
fun CmdContext.argBool(name: String): Boolean = BoolArgumentType.getBool(this, name)
fun CmdContext.areTrue(name: String): Boolean = argBool(name)
fun CmdContext.areFalse(name: String): Boolean = !argBool(name)
// endregion
// region StringArgument
fun string(): ArgumentType<String> = StringArgumentType.string()
fun word(): ArgumentType<String> = StringArgumentType.word()
fun greedyStr(): ArgumentType<String> = StringArgumentType.greedyString()
fun ArgBuilder.argStr(name: String, block: RequiredSetter) { argument(name, string(), block) }
fun ArgBuilder.argWord(name: String, block: RequiredSetter) { argument(name, word(), block) }
fun ArgBuilder.argGreedyStr(name: String, block: RequiredSetter) { argument(name, greedyStr(), block) }
fun CmdContext.argStr(name: String): String = StringArgumentType.getString(this, name)
// endregion

// region ResourceLocationArgument (IdentifierArgument)
fun identifier(): ArgumentType<Id> = ResourceLocationArgument.id()
fun ArgBuilder.argId(name: String, block: RequiredSetter) { argument(name, identifier(), block) }
fun CmdContext.argId(name: String): Id = ResourceLocationArgument.getId(this, name)
// endregion
// region ResourceKeyArgument
fun <T> key(reg: ResourceKey<out Registry<T>>): ArgumentType<ResourceKey<T>> = ResourceKeyArgument.key(reg)
fun <T> ArgBuilder.argKey(reg: ResourceKey<out Registry<T>>, name: String, block: RequiredSetter) { argument(name, key(reg), block) }
fun <T> CmdContext.argKey(
    name: String, reg: ResourceKey<out Registry<T>>,
    exception: DynamicCommandExceptionType = NbtPathArgument.ERROR_NOTHING_FOUND
): Holder.Reference<T> = ResourceKeyArgumentAccessor.resolveKey(this, name, reg, exception)
// endregion
// region BlockPosArgument
fun blockPos(): ArgumentType<Coordinates> = BlockPosArgument.blockPos()
fun ArgBuilder.argBlockPos(name: String, block: RequiredSetter) { argument(name, blockPos(), block) }
fun CmdContext.argBlockPos(name: String): BlockPos = BlockPosArgument.getBlockPos(this, name)
// endregion
// region Vec3dAbsoluteArgument
fun vec3dAbsolute(): ArgumentType<Vector3d> = Vec3dAbsoluteArgument
fun ArgBuilder.argVec3dAbsolute(name: String, block: RequiredSetter) { argument(name, vec3dAbsolute(), block) }
fun CmdContext.argVec3dAbsolute(name: String): Vector3d = Vec3dAbsoluteArgument.getResult(this, name)
// endregion
// region Vec3iAbsoluteArgument
fun vec3iAbsolute(): ArgumentType<Vec3i> = Vec3iAbsoluteArgument
fun ArgBuilder.argVec3iAbsolute(name: String, block: RequiredSetter) { argument(name, vec3iAbsolute(), block) }
fun CmdContext.argVec3iAbsolute(name: String): Vec3i = Vec3iAbsoluteArgument.getResult(this, name)
// endregion

val argTypeReg: Registrar<ArgumentTypeInfo<*, *>> = RegHelper.manager.get(Registries.COMMAND_ARGUMENT_TYPE)
fun <A: ArgumentType<*>, T: ArgumentTypeInfo.Template<A>, I: ArgumentTypeInfo<A, T>> regArgType(
    id: Id, clazz: KClass<out A>,
    info: () -> I
): RegS<I> {
    val r = argTypeReg.register(id, info)
    r.listen { ArgumentTypeInfosAccessor.getByClass()[clazz.java] = it }
    return r
}

// endregion
