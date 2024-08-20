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
}

dependencies {
    compileOnly("org.checkerframework:checker-qual:3.29.0")
    compileOnly ("io.netty:netty-all:4.0.20.Final")
    compileOnly ("org.yaml:snakeyaml:1.18")
    implementation ("net.kyori:adventure-api:4.12.0")
    implementation ("net.kyori:adventure-text-serializer-legacy:4.12.0")
    implementation ("net.kyori:adventure-text-serializer-gson-legacy-impl:4.12.0")
    implementation ("net.kyori:adventure-text-serializer-gson:4.12.0")
    implementation ("space.vectrix.flare:flare:2.0.1")
    implementation ("space.vectrix.flare:flare-fastutil:2.0.1")
    implementation ("it.unimi.dsi:fastutil:8.5.11")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation("com.google.guava:guava:17.0")
    implementation("org.slf4j:slf4j-jdk14:2.0.0-alpha7")
    implementation("org.apache.commons:commons-pool2:2.11.1")
    implementation(project(":compat:snakeyaml-compat-common"))
    implementation(project(":compat:snakeyaml1-compat"))
    implementation(project(":compat:snakeyaml2-compat"))

}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}