@file:Suppress("LocalVariableName", "UnstableApiUsage")

architectury {
    neoForge()
}

configurations {
    val common by creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
    val shadowBundle by creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
    compileClasspath { extendsFrom(common) }
    runtimeClasspath { extendsFrom(common) }
    "developmentNeoForge" { extendsFrom(common) }
}

repositories {
    maven("https://maven.neoforged.net/releases") { name = "NeoForged" }
    maven("https://thedarkcolour.github.io/KotlinForForge/") { name = "Kotlin for Forge"; content { includeGroup("thedarkcolour") } }
    maven("https://maven.su5ed.dev/releases") { name = "Sinytra" } // Sinytra Connector / Forgified Fabric API
}

dependencies {
    val neoforge_version: String by project
    val architectury_api_version: String by project
    neoForge("net.neoforged:neoforge:$neoforge_version")
    modImplementation("dev.architectury:architectury-neoforge:$architectury_api_version")
    "common"(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    "shadowBundle"(project(path = ":common", configuration = "transformProductionNeoForge"))
    val kotlin_for_forge_version: String by project
    if (!kotlin_for_forge_version.startsWith("disabled", ignoreCase = true))
        modCompileOnly("thedarkcolour:kotlinforforge-neoforge:$kotlin_for_forge_version") { isTransitive = false }

    // owo-lib
    val owo_neoforge_version: String by project
    if (!owo_neoforge_version.startsWith("disabled", ignoreCase = true)) {
        val owo_version: String by project
        val owo: String
        = if (owo_neoforge_version.startsWith("as", ignoreCase = true))
            owo_version else owo_neoforge_version
        modImplementation("io.wispforest:owo-lib-neoforge:$owo")
    }
}
