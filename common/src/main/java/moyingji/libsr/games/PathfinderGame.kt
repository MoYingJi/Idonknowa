package moyingji.libsr.games

import moyingji.lib.core.*
import moyingji.lib.math.Vec2i

class PathfinderGame(
    val player: Vec2i,
    val exit: Vec2i,
    val length: Int? = null,
    val canPassExit: Boolean = false,
    val canPassNull: Boolean = false,
    val mapOrder: MapOrder = MapOrder.XA,
    val map: List<List<Slot>>
) {
    fun accept(dirs: List<Direction2D>): Boolean {
        if (length != null && dirs.size != length) return false
        val solver = Solver(this)
        for ((i, d) in dirs.withIndex()) while (solver.accept(d))
            if (!canPassExit && // 设置了不可在完成前经过终点
                dirs.lastIndex != i && // 非当前最后一步
                solver.isFinished) return false
        return solver.isFinished
    }

    fun serializeMap(): String {
        val sb = StringBuilder()
        for (y in map.indices) {
            for (x in map[y].indices)
                sb.append(map[y][x].serialize())
            sb.appendLine() }
        return sb.toString()
    }

    companion object { fun serializeMap(sm: String): List<List<Slot>> {
        val map: MutableList<MutableList<Slot>> = mutableListOf()
        for (line in sm.lines()) {
            val lineMap: MutableList<Slot> = mutableListOf()
            for (char in line) {
                var slot: Slot? = null
                for (serializer in Slot.serializers) {
                    slot = serializer(char)
                    if (slot != null) break }
                if (slot == null) throw IllegalArgumentException()
                lineMap.add(slot) }
            map.add(lineMap) }
        return map
    } }


    data class SlotArgs(
        val sx: Int, val sy: Int,
        val px: Int, val py: Int,
        val dir: Direction2D,
        val game: PathfinderGame
    )

    interface Slot {
        fun canPass(args: SlotArgs): Boolean
        fun serialize(): Char

        object Empty : Slot {
            override fun canPass(args: SlotArgs): Boolean = true
            override fun serialize(): Char = ' '
        }
        object Wall : Slot {
            override fun canPass(args: SlotArgs): Boolean = false
            override fun serialize(): Char = '#'
        }

        class Number(var left: Int) : Slot {
            override fun canPass(args: SlotArgs): Boolean = if (left > 0)
                true.also { left-- } else false
            override fun serialize(): Char = if (left in 0..9)
                '0'+left else throw IllegalArgumentException()
        }

        companion object {
            val serializers: MutableList<(Char) -> Slot?> = mutableListOf(
                { if (it == ' ') Empty else null },
                { if (it == '#') Wall else null },
                { if (it in '0'..'9') Number(it-'0') else null }
            )
        }
    }

    class Solver(val game: PathfinderGame) {
        var x: Int = game.player.first
        var y: Int = game.player.second

        val isFinished: Boolean get() = x == game.exit.first && y == game.exit.second

        // 此处不判断胜负
        fun accept(direction: Direction2D): Boolean {
            val order = game.mapOrder
            val pair = direction.next(x to y, order)
            val slot = order.accessNullable(game.map, pair)
            val (sx, sy) = pair
            val pass = slot?.canPass(
                SlotArgs(sx, sy, x, y, direction, game)) ?: game.canPassNull
            return if (pass) { x = sx; y = sy; true } else false
        }
    }
}