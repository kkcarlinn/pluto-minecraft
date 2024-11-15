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
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.dmulloy2.net/repository/releases/")
    maven("https://mvn.lumine.io/repository/maven-public/")
    mavenLocal();
}

dependencies {
    compileOnly ("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly ("org.spigotmc:spigot:1.19.4-R0.1-SNAPSHOT")
    implementation ("io.netty:netty-all:4.0.23.Final")
    compileOnly ("net.kyori:adventure-text-serializer-gson:4.13.0")
    compileOnly ("net.kyori:adventure-text-serializer-plain:4.13.0")
    implementation ("com.googlecode.json-simple:json-simple:1.1.1")
    implementation ("net.bytebuddy:byte-buddy:1.14.3")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
