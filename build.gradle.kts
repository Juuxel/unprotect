plugins {
    java
    `maven-publish`
    id("com.diffplug.spotless") version "6.5.1"
}

group = "io.github.juuxel"
version = "0.0.6"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://maven.minecraftforge.net")
}

dependencies {
    compileOnly("org.jetbrains:annotations:23.0.0")
    compileOnly("cpw.mods:modlauncher:9.0.4")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("cpw.mods:modlauncher:9.0.4")
}

tasks {
    jar {
        from("LICENSE")
    }

    "sourcesJar"(Jar::class) {
        from("LICENSE")
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
