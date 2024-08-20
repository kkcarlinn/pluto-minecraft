plugins {
    id("java")
}

group = "br.com.plutomc"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    compileOnly("com.github.skipdevelopment:pluto-spigot:1.0")
    implementation(project(":core"))
    implementation(project(":viaversion-api"))
    implementation(project(":viaversion-bukkit"))
    implementation(project(":viarewind"))
    implementation(project(":protocollib"))
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
