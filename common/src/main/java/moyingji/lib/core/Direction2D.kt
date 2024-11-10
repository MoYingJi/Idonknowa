package moyingji.lib.core

import moyingji.lib.api.Immutable
import moyingji.lib.math.*

enum class Direction2D(
    val mapStep: Vec2i
) {
    W(0  to -1),
    A(-1 to  0),
    S(0  to  1),
    D(1  to  0);

    val reverse: Direction2D get() = when (this) { W -> S; A -> D; S -> W; D -> A }
    val next: Direction2D get() = when (this) { W -> A; A -> S; S -> D; D -> A }
    val last: Direction2D get() = when (this) { W -> D; D -> S; S -> A; A -> W }

    val isHorizontal: Boolean get() = this in setOf(A, D)
    val isVertical: Boolean get() = this in setOf(W, S)

    fun mapStep(order: MapOrder): Vec2i {
        val ds = order.elementOrder.applyDiff()
        return order.indexOrder.apply(mapStep * ds)
    }

    fun next(
        @Immutable current: Vec2i = 0 to 0,
        order: MapOrder = MapOrder.XA,
        step: Int = 1
    ): Vec2i = current + (mapStep(order) * step)
}