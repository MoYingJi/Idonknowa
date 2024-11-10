package moyingji.idonknowa.mixink

import moyingji.idonknowa.*
import moyingji.idonknowa.all.ModItem
import moyingji.idonknowa.core.Events
import moyingji.idonknowa.lang.text
import moyingji.idonknowa.util.*
import moyingji.lib.core.PropIn
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player

object LivingEntityMixinImpl {
    fun beforeHurt(
        living: LivingEntity, source: DamageSource, amount: PropIn<Float>
    ) {
        val oa = amount.value
        Events.Entity.LIVING_HURT.invoker().invoke(living, source, amount)
        val ra = amount.value
        if (living is Player && living.offhandItem.isOf(ModItem.TEST_ITEM))
            living.displayClientMessage(("[${platform()} - LIVING_HURT]:" +
                " ${source.type().msgId} $oa -> $ra").text().withStyle(Formatting.GRAY), false)
    }
}