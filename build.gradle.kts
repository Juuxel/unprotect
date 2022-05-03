plugins {
    java
    `maven-publish`
    id("com.diffplug.spotless") version "6.5.1"
}

group = "io.github.juuxel"
version = "0.0.7"

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
    compileOnly("org.jetbrains:annotations:23.0.0")
    compileOnly("cpw.mods:modlauncher:8.0.9")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("cpw.mods:modlauncher:8.0.9")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"

        if (JavaVersion.current().isJava9Compatible) {
            options.release.set(8)
        }
    }

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
