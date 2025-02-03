@file:Suppress("LocalVariableName")
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.architectury.plugin.ArchitectPluginExtension
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import net.fabricmc.loom.api.LoomGradleExtensionAPI as Loom

plugins {
    idea
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.architectury.plugin)
    alias(libs.plugins.architectury.loom) apply false
    alias(libs.plugins.shadow) apply false
}

architectury {
    minecraft = property("minecraft_version") as String
}

dependencies {
    compileOnly(libs.kotlin.script.runtime)
}

allprojects {
    group = property("maven_group") as String
    version = property("mod_version") as String
}

val kotlinTarget = JvmTarget.JVM_21
val javaVersion = JavaVersion.VERSION_21
val javaRelease = 21

subprojects {
    with(rootProject.libs.plugins) { listOf(
        architectury.plugin,
        architectury.loom,
        kotlin.jvm,
        kotlin.serialization,
        kotlin.ksp
    ) }.forEach { apply(plugin = it.get().pluginId) }

    base { archivesName = "Idonknowa-${project.name}" }

    configure<Loom> {
        silentMojangMappingsLicense()
    }

    fun MavenArtifactRepository.includeGroup(vararg groups: String) {
        content { for (g in groups) includeGroup(g) }
    }
    repositories {
        mavenLocal()
        flatDir { dir("libs") }
        maven("https://maven.parchmentmc.org") { name = "ParchmentMC"; includeGroup("org.parchmentmc.data") }
        maven("https://maven.quiltmc.org/repository/release/") { name = "QuiltMC"; includeGroup("org.quiltmc") }
        maven("https://api.modrinth.com/maven") { name = "Modrinth"; includeGroup("maven.modrinth") }
        mavenCentral()
        maven("https://maven.wispforest.io/releases/") { name = "WispForest" }
    }

    dependencies {
        fun include(dep: Any): Dependency?
        = if (this@subprojects.name != "common")
            "include"(dep) else null

        // Minecraft
        val minecraft_version: String by project
        "minecraft"("::$minecraft_version")

        // region Mappings
        fun String.isEnabled(): Boolean = !startsWith("disabled", ignoreCase = true)
        val yarn_mappings: String by project
        val quilt_mappings: String by project
        val yarn_mappings_patch: String by project
        val parchment_mappings: String by project
        val crane_mappings: String by project
        @Suppress("UnstableApiUsage")
        "mappings"(project.the<Loom>().layered {
            // 主要映射选择
            when {
                yarn_mappings.isEnabled() ->
                    mappings("net.fabricmc:yarn:$yarn_mappings:v2")
                quilt_mappings.isEnabled() ->
                    mappings("org.quiltmc:quilt-mappings:$quilt_mappings:intermediary-v2")
                else -> officialMojangMappings()
            }
            // 映射补丁
            if (yarn_mappings_patch.isEnabled())
                mappings("dev.architectury:yarn-mappings-patch-neoforge:$yarn_mappings_patch")
            if (parchment_mappings.isEnabled())
                parchment("org.parchmentmc.data:parchment-$parchment_mappings@zip")
            if (crane_mappings.isEnabled())
                crane("dev.architectury:crane:$crane_mappings")
        })
        // endregion

        // Kotlin Module Impl
        val fabric_kotlin_version: String by project
        implementation("net.fabricmc:fabric-language-kotlin:$fabric_kotlin_version")

        // Arrow Kt
        val arrow_kt_modules: String by project
        val arrow_kt_version: String by project
        val arktm = arrow_kt_modules.split(",")
        arktm.forEach {
            implementation("io.arrow-kt:arrow-$it:$arrow_kt_version")
            include("io.arrow-kt:arrow-$it:$arrow_kt_version")
        }
        if ("optics" in arktm)
            ksp("io.arrow-kt:arrow-optics-ksp-plugin:$arrow_kt_version")
    }

    tasks.processResources {
        val files = listOf("fabric.mod.json", "META-INF/neoforge.mods.toml")
        // 捕获由文件不存在引起的 ClosedFileSystemException
        for (f in files) runCatching { filesMatching(f) {
            expand("version" to project.version)
        } }.onFailure { it.printStackTrace() }
    }

    kotlin {
        this.compilerOptions.jvmTarget = kotlinTarget
    }

    java {
        withSourcesJar()
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    tasks.withType<JavaCompile>().configureEach { options.release = javaRelease }

    afterEvaluate { if (project.name != "common") {
        // Platformed Modules
        apply(plugin = rootProject.libs.plugins.shadow.get().pluginId)

        tasks.withType<ShadowJar> {
            configurations = listOf(project.configurations.named("shadowBundle").get())
            archiveClassifier = "dev-shadow"
            // mergeServiceFiles()
        }
        tasks.withType<RemapJarTask> {
            inputFile.set(tasks.named<ShadowJar>("shadowJar").get().archiveFile)
        }

        configure<ArchitectPluginExtension> {
            platformSetupLoomIde()
        }
    } }
}
