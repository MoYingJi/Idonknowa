package moyingji.idonknowa.mixin.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(CommandContext.class)
interface CommandContextAccessor {
    @Accessor(value = "arguments", remap = false)
    Map<String, ParsedArgument<?, ?>> getArguments();
}

