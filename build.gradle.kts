plugins {
    java
    id("com.gradleup.shadow") version "8.3.0"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
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

tasks.named<Jar>("jar") {
    archiveClassifier.set("")
}

tasks.shadowJar {
    archiveFileName.set("LifestealPlus.jar")
    relocate("com.zaxxer.hikari", "dev.lifesteal.libs.hikari")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}