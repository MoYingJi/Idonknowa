pluginManagement.repositories {
    maven("https://maven.fabricmc.net/")
    maven("https://maven.architectury.dev/")
    maven("https://files.minecraftforge.net/maven/") { content {
        includeGroup("net.minecraftforge")
        includeGroup("de.oceanlabs.mcp")
    } }
    gradlePluginPortal()
    mavenCentral()
}

dependencyResolutionManagement.versionCatalogs
    .create("libs").from(files("./libs.versions.toml"))

rootProject.name = "idonknowa"

include("common")
include("fabric")
include("neoforge")