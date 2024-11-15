import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.ByteArrayOutputStream

plugins {
    id("java")
    id("net.minecrell.plugin-yml.bukkit") version "0.3.0"
}

group = "br.com.plutomc.lobby"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lobby-core"))
}

val shadowJarTask = tasks.getByName<ShadowJar>("shadowJar")

tasks.register<Copy>("copyToPluginsDir") {
    dependsOn(shadowJarTask)
    from(shadowJarTask.outputs.files)
    into("C:/pluto/lobbybw/plugins/")
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

bukkit {
    name = "lobbyhost"
    version = "1.0.0-${getGitCommitId().substring(0, 7)} from ${getGitBranch()} LTS (${getBuildDate()})"
    main = "br.com.plutomc.lobby.bedwars.LobbyMain"
    authors = listOf("unidade")
    description = "Lobby server system (based on [core, core-bukkit, core-*])"
    website = "www.plutomc.com.br"
}
