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
    val modLauncher8 = "8.0.9"
    val modLauncher9 = "9.0.4"

    compileOnly("org.jetbrains:annotations:23.0.0")
    compileOnly("cpw.mods:modlauncher:$modLauncher8")

    "modularityCompileOnly"("org.jetbrains:annotations:23.0.0")
    "modularityCompileOnly"("cpw.mods:modlauncher:$modLauncher9")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("cpw.mods:modlauncher:$modLauncher8")
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
