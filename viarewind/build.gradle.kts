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
}

dependencies {
    compileOnly ("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly ("io.netty:netty-all:4.0.20.Final")
    implementation ("net.kyori:adventure-text-serializer-legacy:4.9.3")
    implementation ("net.kyori:adventure-text-serializer-gson-legacy-impl:4.9.3")
    implementation ("net.kyori:adventure-text-serializer-gson:4.9.3")
    implementation ("space.vectrix.flare:flare:2.0.0")
    implementation ("space.vectrix.flare:flare-fastutil:2.0.0")
    implementation ("it.unimi.dsi:fastutil:8.5.6")
    implementation(project(":viaversion-api"))
    implementation("com.google.guava:guava:17.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.slf4j:slf4j-jdk14:2.0.0-alpha7")
    implementation("org.apache.commons:commons-pool2:2.11.1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
