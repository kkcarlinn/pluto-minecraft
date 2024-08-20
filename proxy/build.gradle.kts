import java.io.ByteArrayOutputStream

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.minecrell.plugin-yml.bungee") version "0.3.0"
}

group = "br.com.plutomc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":core"))
    compileOnly("com.github.skipdevelopment:pluto-bungee:1.0")
}

fun getGitCommitId(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}

val currentCommitId = getGitCommitId()

fun getGitBranch(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}

val currentBranch = getGitBranch()

fun getBuildDate() : String{
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "show", "--no-patch", "--format=%ci")
        standardOutput = stdout
    }
    return stdout.toString().trim().replace(" -0300", "").replace("-", "/")
}
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

bungee {
    name = "proxy"
    main = "br.com.plutomc.core.bungee.BungeeMain"
    version = "1.0.0-${getGitCommitId().substring(0, 7)} from ${getGitBranch()} LTS (${getBuildDate()})"
    author = "unidade"
}
