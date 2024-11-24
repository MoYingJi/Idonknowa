@file:Suppress("LocalVariableName", "UnstableApiUsage")

architectury {
    fabric()
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
    "developmentFabric" { extendsFrom(common) }
}

dependencies {
    val fabric_loader_version: String by project
    val fabric_api_version: String by project
    val architectury_api_version: String by project
    modImplementation("net.fabricmc:fabric-loader:$fabric_loader_version")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_api_version")
    modImplementation("dev.architectury:architectury-fabric:$architectury_api_version")
    "common"(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    "shadowBundle"(project(path = ":common", configuration = "transformProductionFabric"))
    val fabric_kotlin_version: String by project
    if (!fabric_kotlin_version.startsWith("disabled", ignoreCase = true))
        modImplementation("net.fabricmc:fabric-language-kotlin:$fabric_kotlin_version")
}

fabricApi {
    configureDataGeneration {
        outputDirectory = project(":common").file("src/generated/resources")
    }
}