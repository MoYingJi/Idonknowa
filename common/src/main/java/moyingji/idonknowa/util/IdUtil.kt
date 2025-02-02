package moyingji.idonknowa.util

import moyingji.idonknowa.Idonknowa
import net.minecraft.util.Identifier

fun String.id(namespace: String? = Idonknowa.MOD_ID): Identifier
= Identifier.of(namespace ?: Identifier.DEFAULT_NAMESPACE, this)