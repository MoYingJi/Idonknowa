package moyingji.idonknowa.mia.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import moyingji.idonknowa.core.Events;
import moyingji.idonknowa.core.events.PlayerDropItemModifier;
import moyingji.idonknowa.items.ModifyStackDrop;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
abstract class PlayerMixin {
    @ModifyReturnValue(
        method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At("RETURN"))
    final @Nullable ItemEntity dropReturn(
        @Nullable ItemEntity original,
        final ItemStack droppedItem,
        final boolean dropAround,
        final boolean includeThrowerName
    ) {
        final var self = (Player) (Object) this;
        final var args = new PlayerDropItemModifier(
            original, droppedItem, self,
            dropAround, includeThrowerName
        );
        if (droppedItem.getItem() instanceof ModifyStackDrop d)
            original = d.modifyStackDrop(
                original, droppedItem, self,
                dropAround, includeThrowerName);
        Events.Player.INSTANCE.getDROP_ITEM().invoker().invoke(args);
        if (args.getOriginal() != original) original = args.getOriginal();
        return original;
    }
}
