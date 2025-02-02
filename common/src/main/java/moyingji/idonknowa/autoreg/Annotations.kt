package moyingji.idonknowa.autoreg

import kotlin.reflect.KCallable

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class RegName(val name: String)

fun KCallable<*>.regName(action: (String) -> String): String {
    val name: RegName? = this.annotations.find { it is RegName } as? RegName
    return name?.name ?: this.name.let(action)
}