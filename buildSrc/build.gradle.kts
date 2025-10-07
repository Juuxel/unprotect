plugins {
    id("java-gradle-plugin")
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("com.diffplug.spotless:com.diffplug.spotless.gradle.plugin:6.5.1")
}

gradlePlugin {
    plugins.register("unprotect") {
        id = "io.github.juuxel.unprotect"
        implementationClass = "juuxel.unprotect.gradle.UnprotectPlugin"
    }
}

tasks.withType<JavaCompile> {
    options.release.set(21)
}
