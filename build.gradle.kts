plugins {
    id("java")
}

group = "br.com.plutomc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

}

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.24")
        annotationProcessor("org.projectlombok:lombok:1.18.24")
        implementation("com.google.guava:guava:17.0")
        implementation("com.google.code.gson:gson:2.10.1")
        implementation("org.slf4j:slf4j-jdk14:2.0.0-alpha7")
        implementation("com.github.docker-java:docker-java:3.4.0")
        implementation("com.github.docker-java:docker-java-transport-httpclient5:3.2.13")
        implementation("org.slf4j:slf4j-api:1.7.36")
        implementation("org.apache.commons:commons-pool2:2.11.1")
    }


    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = "8"
        targetCompatibility = "8"
    }
}