plugins {
    java
    `maven-publish`
    id("com.diffplug.spotless") version "6.5.1"
}

group = "io.github.juuxel"
version = "0.0.7"

val modularity = sourceSets.register("modularity") {
    java {
        srcDirs(sourceSets.main.map { it.java.srcDirs })
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://maven.minecraftforge.net")
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.modlauncher8)

    "modularityCompileOnly"(libs.jetbrains.annotations)
    "modularityCompileOnly"(libs.modlauncher9)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.modlauncher8)
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(8)
    }

    "compileModularityJava"(JavaCompile::class) {
        options.release.set(16)
    }

    jar {
        from("LICENSE")
        from(modularity.map { it.output }) {
            include("module-info.class")
        }
    }

    "sourcesJar"(Jar::class) {
        from("LICENSE")
        from(modularity.map { it.allSource }) {
            include("module-info.java")
        }
    }

    test {
        useJUnitPlatform()
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

spotless {
    java {
        licenseHeaderFile("HEADER.txt")
    }
}
