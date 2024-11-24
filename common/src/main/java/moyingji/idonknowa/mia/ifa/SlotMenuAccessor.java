package moyingji.idonknowa.mia.ifa;

import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public interface SlotMenuAccessor {
    default @Nullable AbstractContainerMenu idonknowa$getMenu() { throw new AssertionError(); }
    default void idonknowa$setMenu(@Nullable AbstractContainerMenu menu) { throw new AssertionError(); }
}
