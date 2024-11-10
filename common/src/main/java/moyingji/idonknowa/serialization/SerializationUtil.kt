package moyingji.idonknowa.serialization

import java.util.*

typealias KJson = kotlinx.serialization.json.Json

fun <T, R: Any> ((T) -> R?).mapOptional(): (T) -> Optional<R>
= { t -> Optional.ofNullable(this(t)) }