package moyingji.idonknowa.mixin.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import moyingji.idonknowa.Idonknowa;
import moyingji.idonknowa.command.SuggestWithArgType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Commands.class)
abstract class CommandsMixin {
    @Inject(method = "argument", at = @At("RETURN"))
    private static <T> void argument(
        String name, ArgumentType<T> type,
        CallbackInfoReturnable<RequiredArgumentBuilder<CommandSourceStack, T>> cir
    ) {
        final var r = cir.getReturnValue();
        if (type instanceof SuggestWithArgType s && r.getSuggestionsProvider() == null)
            r.suggests((c, b) -> {
                final var a = (CommandContextAccessor) c;
                @Nullable final var sr = a.getArguments().get(name);
                final var in = sr == null ? "" : sr.getRange().get(c.getInput());
                return s.listSuggestions(in, c, b);
            });
    }
}
