package moyingji.idonknowa.entity

import moyingji.idonknowa.Idonknowa
import moyingji.lib.core.PropertyMap.map
import net.minecraft.network.syncher.*
import net.minecraft.network.syncher.SynchedEntityData.Builder
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.*
import net.minecraft.world.level.Level

abstract class StagableMob(type: EntityType<out StagableMob>, level: Level) : Mob(type, level) {
    companion object {
        val DATA_STAGE: EntityDataAccessor<Byte> = SynchedEntityData
            .defineId(StagableMob::class.java, EntityDataSerializers.BYTE)
    }
    override fun defineSynchedData(builder: Builder) {
        super.defineSynchedData(builder)
        builder.define(DATA_STAGE, 0)
    }
    abstract val maxStage: UByte
    var currentStage: UByte by EntityDataValue(DATA_STAGE).map(Byte::toUByte, UByte::toByte)

    open val changingStageHealingSpeed: Float = 20F
    var isChangingStage: Boolean = false
    open fun nextStage() {
        health = changingStageHealingSpeed
        isChangingStage = true
        currentStage ++
    }
    open fun changingStageTick() {
        if (isDeadOrDying) health = changingStageHealingSpeed
        else heal(changingStageHealingSpeed)
    }
    open fun finnishChangingStage() { isChangingStage = false }
    override fun tick() {
        if (isChangingStage) {
            changingStageTick()
            if (health == maxHealth) finnishChangingStage()
        } else super.tick()
    }
    override fun die(cause: DamageSource) {
        if (isChangingStage) Idonknowa.debug("What the fuck? (die when changing stage)")
        else if (currentStage < maxStage) nextStage()
        else super.die(cause)
    }
    override fun hurt(source: DamageSource, amount: Float): Boolean {
        if (isChangingStage) return false
        return super.hurt(source, amount)
    }
}