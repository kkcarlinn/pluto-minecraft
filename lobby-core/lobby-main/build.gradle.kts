import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
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

val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
val details = versionDetails()

val shadowJarTask = tasks.getByName<ShadowJar>("shadowJar")

tasks.register<Copy>("copyToPluginsDir") {
    dependsOn(shadowJarTask)
    from(shadowJarTask.outputs.files)
    into("C:/pluto/lobby-1/plugins/")
}

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
    version = "1.0.0-${details.gitHash.substring(0, 7)} from ${details.branchName} LTS (${getBuildDate()})"
    main = "br.com.plutomc.lobby.main.LobbyMain"
    authors = listOf("unidade")
    description = "Lobby server system (based on [core, core-bukkit, core-*])"
    website = "www.plutomc.com.br"
}
