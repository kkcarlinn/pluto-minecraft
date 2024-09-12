import java.io.ByteArrayOutputStream

plugins {
    id("java")
    id("net.minecrell.plugin-yml.bukkit") version "0.3.0"
}

group = "br.com.plutomc.arcade"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":arcade"))
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
    name = "Arcade"
    version = "1.0.0-${getGitCommitId().substring(0, 7)} from ${getGitBranch()} LTS (${getBuildDate()})"
    main = "br.com.plutomc.game.bedwars.GameMain"
    authors = listOf("unidade")
    description = "Arcade server system (based on [core, core-bukkit, core-*, arcade])"
    website = "www.plutomc.com.br"
}