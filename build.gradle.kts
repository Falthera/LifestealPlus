plugins {
    java
    id("io.paperweight.loaddev") version "1.6.1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholder/")
    maven("https://jitpack.io")
}

dependencies {
    paperDevBundle("io.papermc.paper:paper-dev-bundle:1.21.11-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("net.milkbowl.vault:Vault:1.7.3")
    implementation("com.zaxxer:HikariCP:5.1.0")
    runtimeOnly("com.mysql:mysql-connector-j:8.4.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("Lifesteal+.jar")
    relocate("com.zaxxer.hikari", "dev.lifesteal.libs.hikari")
    dependencies {
        include(dependency("com.zaxxer:HikariCP:5.1.0"))
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
