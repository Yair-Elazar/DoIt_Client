plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0" // מאפשר את kotlinx.serialization
    id("application")
}

group = "org.YairElazar"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val javaFxVersion = "21"
val osName = System.getProperty("os.name").lowercase()
val platform = when {
    osName.contains("mac") -> "mac"
    osName.contains("win") -> "win"
    osName.contains("linux") -> "linux"
    else -> throw GradleException("Unsupported OS: $osName")
}
val ktorVersion = "2.3.8"
dependencies {
    // JavaFX
    implementation("org.openjfx:javafx-base:$javaFxVersion:$platform")
    implementation("org.openjfx:javafx-controls:$javaFxVersion:$platform")
    implementation("org.openjfx:javafx-graphics:$javaFxVersion:$platform")
    implementation("org.openjfx:javafx-fxml:$javaFxVersion:$platform")

    // Ktor Client
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-json:2.2.3")
    implementation("io.ktor:ktor-client-serialization:2.2.3")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    implementation("org.slf4j:slf4j-simple:1.7.36")



    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // 🚀 קורוטינות עם JavaFX
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.7.3")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("org.YairElazar.ui.LoginScreenKt")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
