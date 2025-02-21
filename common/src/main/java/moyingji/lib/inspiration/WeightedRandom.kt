package moyingji.lib.inspiration

class WeightedRandom<T> (
    val random: (until: Int) -> Int,
    val weights: MutableMap<T, Int> = mutableMapOf()
) {
    fun generate(
        total: Int = weights.values.sum()
    ): T {
        val rand = random.invoke(total)
        var sum = 0
        for ((item, weight) in weights) {
            sum += weight
            if (rand < sum) return item
        }
        throw IllegalStateException()
    }

    fun completeTo(total: Int, default: T) {
        val t = weights.values.sum()
        weights += default to (total - t)
    }

    fun add(item: T, weight: Int) { weights += item to weight }
    fun add(item: T, weight: Int, limitTotal: Int) {
        val t = weights.values.sum()
        weights += item to if (t + weight > limitTotal)
             limitTotal - t else weight
    }
}
