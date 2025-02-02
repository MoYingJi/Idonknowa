@file:Suppress("LocalVariableName")

architectury {
    val enabled_platforms: String by project
    common(enabled_platforms.split(","))
}

sourceSets.main {
    java.srcDirs("src/generated/java")
    resources.srcDirs("src/generated/resources")
}

dependencies {
    val fabric_loader_version: String by project
    val architectury_api_version: String by project
    modImplementation("net.fabricmc:fabric-loader:$fabric_loader_version")
    modImplementation("dev.architectury:architectury:$architectury_api_version")

    // 当使用 fabric-api 时需要对应 fabric-api 实现
    // Forge 端需要 forgified-fabric-api
    // Quilt 端需要 quilted-fabric-api
    val fabric_api_version: String by project
    if (!fabric_api_version.startsWith("disabled", ignoreCase = true))
        modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_api_version")

    // owo lib impl
    val owo_version: String by project
    modImplementation("io.wispforest:owo-lib:$owo_version")
}