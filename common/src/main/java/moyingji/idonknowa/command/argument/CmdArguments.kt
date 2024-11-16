package moyingji.idonknowa.command.argument

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import moyingji.idonknowa.Idonknowa.id
import moyingji.idonknowa.command.regArgType
import net.minecraft.commands.synchronization.SingletonArgumentInfo
import net.minecraft.core.Vec3i
import org.joml.Vector3d

object Vec3dAbsoluteArgument : ArgumentType<Vector3d> {
    override fun parse(reader: StringReader): Vector3d
    = Vector3d(reader.readDouble(), reader.readDouble(), reader.readDouble())

    val type = regArgType(
        "absolute_vec3d".id, Vec3dAbsoluteArgument::class
    ) { SingletonArgumentInfo.contextAware { this } }

    fun getResult(context: CommandContext<*>, name: String): Vector3d
    = context.getArgument(name, Vector3d::class.java)
}

object Vec3iAbsoluteArgument : ArgumentType<Vec3i> {
    override fun parse(reader: StringReader): Vec3i
    = Vec3i(reader.readInt(), reader.readInt(), reader.readInt())

    val type = regArgType(
        "vec3i_absolute".id, Vec3iAbsoluteArgument::class
    ) { SingletonArgumentInfo.contextAware { this } }

    fun getResult(context: CommandContext<*>, name: String): Vec3i
    = context.getArgument(name, Vec3i::class.java)
}