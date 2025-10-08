plugins {
    id("io.github.juuxel.unprotect")
}

group = "io.github.juuxel"
version = "2.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
    api(libs.asm.tree)
    implementation(libs.log4j.api)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.modlauncher4)
}

tasks {
    withType<JavaCompile> {
        options.release.set(8)
    }

    jar {
        manifest {
            attributes(
                "Automatic-Module-Name" to "io.github.juuxel.unprotect",
                "Premain-Class" to "juuxel.unprotect.UnprotectAgent",
            )
        }
    }

    test {
        useJUnitPlatform()
    }
}
