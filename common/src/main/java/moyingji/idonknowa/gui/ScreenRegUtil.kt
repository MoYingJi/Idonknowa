package moyingji.idonknowa.gui

import dev.architectury.event.events.client.ClientLifecycleEvent.CLIENT_SETUP
import dev.architectury.platform.Platform
import dev.architectury.registry.menu.MenuRegistry
import dev.architectury.utils.Env
import moyingji.idonknowa.Text
import moyingji.idonknowa.core.RegHelper
import moyingji.lib.util.typed
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.MenuAccess
import net.minecraft.core.registries.Registries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.MenuType

class MenuRegHelper<R: MenuType<*>>(
    initializer: () -> R
) : RegHelper<R>(Registries.MENU.typed(), initializer)

fun <M: Menu, S> regMenu(
    initMenu: (syncId: Int, Inventory, FriendlyByteBuf) -> M,
    initScreen: () -> (M, Inventory, title: Text) -> S
): MenuRegHelper<MenuType<M>> where S: Screen, S: MenuAccess<M> {
    val menuReg = MenuRegHelper { MenuRegistry.ofExtended(initMenu) }
    if (Platform.getEnvironment() == Env.CLIENT) CLIENT_SETUP.register {
        MenuRegistry.registerScreenFactory(menuReg.regs.value(), initScreen()) }
    return menuReg
}

fun <M: Menu, S> regMenu(
    initMenu: (syncId: Int, Inventory) -> M,
    initScreen: () -> (M, Inventory, title: Text) -> S,
): MenuRegHelper<MenuType<M>> where S: Screen, S: MenuAccess<M> = regMenu(
    { s, i, _ -> initMenu(s, i) },
    initScreen
)