package moyingji.lib.inspiration

interface ExpectsFrom<T, R> { infix fun from(t: T): R }
interface ExpectsTo<T, R> { infix fun to(t: T): R }
