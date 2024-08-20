plugins {
    id("java")
}

group = "br.com.plutomc"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://repo.destroystokyo.com/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    compileOnly ("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly ("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    implementation ("io.netty:netty-all:4.1.68.Final")
    implementation ("net.kyori:adventure-text-serializer-legacy:4.9.3")
    implementation ("net.kyori:adventure-text-serializer-gson-legacy-impl:4.9.3")
    implementation ("net.kyori:adventure-text-serializer-gson:4.9.3")
    implementation ("space.vectrix.flare:flare:2.0.0")
    implementation ("space.vectrix.flare:flare-fastutil:2.0.0")
    implementation ("it.unimi.dsi:fastutil:8.5.6")
    implementation ("org.javassist:javassist:3.28.0-GA")
    implementation(project(":viaversion-api"))
    implementation("com.google.guava:guava:17.0")
    implementation("org.slf4j:slf4j-jdk14:2.0.0-alpha7")
    implementation("org.apache.commons:commons-pool2:2.11.1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
