plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.2.0"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
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

tasks.named("jar") {
    archiveClassifier.set("")
}

shadowJar {
    archiveFileName.set("Lifesteal+.jar")
    relocate("com.zaxxer.hikari", "dev.lifesteal.libs.hikari")
    dependencies {
        include(dependency("com.zaxxer:HikariCP:5.1.0"))
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
