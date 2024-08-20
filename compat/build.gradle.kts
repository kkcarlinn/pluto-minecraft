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
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
