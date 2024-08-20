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
    compileOnly("com.github.skipdevelopment:pluto-bungee:1.0")
    compileOnly("com.github.skipdevelopment:pluto-spigot:1.0")
    implementation("org.mongodb:mongo-java-driver:3.12.13")
    implementation("redis.clients:jedis:2.9.0")
    implementation("com.google.code.gson:gson:2.10")
}