import java.io.ByteArrayOutputStream

plugins {
    id("java")
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