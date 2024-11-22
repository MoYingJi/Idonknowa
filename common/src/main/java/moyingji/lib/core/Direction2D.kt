package moyingji.lib.core

import moyingji.lib.api.Immutable
import moyingji.lib.math.*

enum class Direction2D(
    /** 默认方向是 [MapOrder.XA], 需要选择方向调用 `mapStep(order)` */
    val mapStep: Vec2i
) {
    W(0  to -1),
    A(-1 to  0),
    S(0  to  1),
    D(1  to  0);

    val reverse: Direction2D get() = when (this) { W -> S; A -> D; S -> W; D -> A }
    val next: Direction2D get() = when (this) { W -> A; A -> S; S -> D; D -> W }
    val last: Direction2D get() = when (this) { W -> D; D -> S; S -> A; A -> W }

    val isHorizontal: Boolean get() = this == A || this == D
    val isVertical: Boolean get() = this == W || this == S

    /**
     * 此 [Direction2D] 在 [order] 下移动一个单位
     * 默认情况下相当于 [mapStep]
     * @param order 移动方向
     */
    fun mapStep(order: MapOrder = MapOrder.XA): Vec2i {
        if (order == MapOrder.XA) return mapStep // 默认情况提前返回
        val step = order.indexOrder.apply(mapStep)
        val ds = order.elementOrder.apply()
        val dc = order.childOrder.apply()
        return step.first * ds to step.second * dc
    }

    /**
     * 移动一个单位 默认情况相当于 [mapStep]
     * @param current 当前坐标 (不可变)
     * @param order 移动方向
     * @param step 移动步数
     */
    fun next(
        @Immutable current: Vec2i = 0 to 0,
        order: MapOrder = MapOrder.XA, step: Int = 1
    ): Vec2i = current + (mapStep(order) * step)
}