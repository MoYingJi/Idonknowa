package moyingji.idonknowa

import com.mojang.logging.LogUtils
import dev.architectury.injectables.annotations.ExpectPlatform
import dev.architectury.platform.Platform
import moyingji.idonknowa.all.*
import moyingji.idonknowa.command.ModCommand
import moyingji.idonknowa.core.*
import moyingji.idonknowa.datagen.*
import moyingji.idonknowa.entity.StagableMob
import moyingji.idonknowa.items.initItemInterfacesKt
import moyingji.idonknowa.recipe.ModRecipe
import moyingji.idonknowa.rs.poi.ModVillager
import moyingji.idonknowa.rs.tag.ModTag
import moyingji.idonknowa.util.TooltipUtil
import moyingji.idonknowa.world.virtual.VirtualManager
import moyingji.lib.util.titlecase
import net.fabricmc.api.EnvType.*
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger
import org.slf4j.event.Level
import java.io.File

typealias platformed = ExpectPlatform

@Suppress("NOTHING_TO_INLINE")
object Idonknowa {
    const val MOD_ID: String = "idonknowa"
    inline val logger: Logger get() = LogUtils.getLogger()

    fun init() {
        info("Hello Idonknowa!")
        if (Platform.isFabric()) info("Hello Idonknowa is Fabric!")
        if (Platform.isForgeLike()) info("Hello Idonknowa is ForgeLike!")
        if (isDatagen) info("Hello Idonknowa is FabricAPI DataGen!")
        ModItem // init
        ModBlock // init
        ModTab // init
        ModTag // init
        ModRecipe // init
        ModVillager // init
        ModCommand // init

        Events.regEvents()
        initItemInterfacesKt()
        Refinable // init
        TooltipUtil // init
        StagableMob // init

        VirtualManager.dataState // serialize load
    }

    @Environment(CLIENT)
    fun initClient(client: Minecraft = Minecraft.getInstance()) {
        info("Hello Idonknowa Client")
        if (client.isSameThread) threadClient = Thread.currentThread()
    }

    val isDatagen: Boolean = System.getProperty("fabric-api.datagen") != null
    @Environment(SERVER) fun dataGen(gener: DataGener) {
        assert(isDatagen)
        info("Hello Idonknowa Data Gen!")
        // genCodes
        val codes = File("")
            .absoluteFile.parentFile.parentFile.parentFile
            .let { File(it, "common/src/generated/java") }
        moyingji.idonknowa.lang.genCode(codes)
        TagsProviderGener.genCode(codes)
        // genResources
        val f = System.getProperty("fabric-api.datagen.output-dir")
        info("Idonknowa Datagen Output Dir: $f")
        val pack: DataPack = gener.createPack()
        pack.addProvider(::ModelProvider)
        LootDataProviders.provide(pack)
        pack.addProvider(::BlockLootTableProvider)
        TagsProvider.provide(pack)
        pack.addProvider(::RecipeProvider)
        pack.addProvider(::AdvancementProvider)
        LangProviders.provide(pack)
    }

    var threadClient: Thread? = null
    var threadServer: Thread? = null
    var currentServer: MinecraftServer? = null

    // region Logging
    internal fun String.processMsg(): String {
        var msg = this
        if (!msg.contains(MOD_ID, ignoreCase = true))
            msg = "[${MOD_ID.titlecase()}] $msg"
        return msg
    }
    internal inline fun info(message: String, level: Level) {
        val logger = logger
        if (!logger.isEnabledForLevel(level)) return
        val msg = message.processMsg()
        when (level) {
            Level.TRACE -> logger.trace(msg)
            Level.DEBUG -> logger.debug(msg)
            Level.INFO -> logger.info(msg)
            Level.WARN -> logger.warn(msg)
            Level.ERROR -> logger.error(msg)
        }
    }
    internal inline fun trace(message: String) { logger.trace(message.processMsg()) }
    internal inline fun debug(message: String) { logger.debug(message.processMsg()) }
    internal inline fun info(message: String) { logger.info(message.processMsg()) }
    internal inline fun warn(message: String) { logger.warn(message.processMsg()) }
    internal inline fun error(message: String) { logger.error(message.processMsg()) }
    // endregion
    // region Tools
    fun String.idOrNull(namespace: String): Id? = if (this.contains(':'))
        Id.tryParse(this) else Id.tryBuild(namespace, this)
    fun String.id(namespace: String): Id = idOrNull(namespace)!!
    val String.id: Id get() = idOrNull(MOD_ID)!!
    val Id.autoString: String get() = if (this.namespace == "minecraft") this.path else this.toString()
    // endregion
}