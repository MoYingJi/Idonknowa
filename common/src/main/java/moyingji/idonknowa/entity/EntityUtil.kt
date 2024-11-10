package moyingji.idonknowa.entity

import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.world.entity.Entity
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class EntityDataValue<T: Any>(
    val type: EntityDataAccessor<T>
) : ReadWriteProperty<Entity, T> {
    override fun getValue(thisRef: Entity, property: KProperty<*>): T
    = thisRef.entityData.get(type)
    override fun setValue(thisRef: Entity, property: KProperty<*>, value: T)
    { thisRef.entityData.set(type, value) }
}