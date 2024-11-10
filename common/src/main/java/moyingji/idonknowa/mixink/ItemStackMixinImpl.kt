package moyingji.idonknowa.mixink

import moyingji.idonknowa.items.StackDamageCanBreak
import net.minecraft.server.level.*
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.*
import java.util.function.Consumer

object ItemStackMixinImpl {
    fun hurtBeforeBreak(
        stack: ItemStack,
        damage: Int,
        level: ServerLevel,
        living: LivingEntity?,
        onBreak: Consumer<Item>,
        ci: CI
    ) {
        val item = stack.item
        if (item is StackDamageCanBreak)
            if (!item.beforeStackBreak(stack, level, living))
                ci.cancel()
    }
}