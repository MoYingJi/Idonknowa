package moyingji.idonknowa.datagen

import moyingji.idonknowa.Idonknowa
import org.intellij.lang.annotations.Language
import java.io.File

object CodeGen {
    fun genCodeIn(
        @Language("kotlin", prefix = "class ") fn: String,
        @Language("kotlin") pkg: String,
        codes: File,
        f: StringBuilder.() -> Unit
    ) {
        val file = File(codes, "$fn.kt")
        Idonknowa.info("genCode in $file")
        if (file.exists()) file.delete()
        val sb = StringBuilder()
        sb.appendLine("// 代码自动生成 请勿更改")
        sb.appendLine("// The code is automatically generated, do not change it.")
        sb.appendLine("@file:Suppress(\"PackageDirectoryMismatch\")")
        sb.appendLine("package $pkg")
        sb.appendLine()
        f(sb)
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        file.writeText(sb.toString())
    }
}