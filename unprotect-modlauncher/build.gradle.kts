plugins {
    id("io.github.juuxel.unprotect")
}

group = rootProject.group
version = rootProject.version

unprotect {
    registerMrjSourceSet {
        javaVersion = 16
    }
}

// Generate a fake FML jar to compile against. We only want one instance of
// the ModLauncher API on the compile classpath, so the other instance is
// represented by an empty jar.
val fakeFancyModLoaderJar = tasks.register<Jar>("fakeFancyModLoaderJar") {
    destinationDirectory.set(layout.buildDirectory.dir("generated/fakeFml"))
    archiveFileName.set("fake_fml.jar")

    manifest {
        attributes("Automatic-Module-Name" to "fml_loader")
    }
}

dependencies {
    implementation(project(":"))
    implementation(libs.modlauncher4)

    "java16CompileOnly"(project(":"))
    "java16CompileOnly"(libs.modlauncher9)
    "java16CompileOnly"(fakeFancyModLoaderJar.map { it.outputs.files })
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    withType<JavaCompile> {
        options.release.convention(8)
    }
}

