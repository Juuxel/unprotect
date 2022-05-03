plugins {
    java
    `maven-publish`
    signing
    id("com.diffplug.spotless") version "6.5.1"
}

group = "io.github.juuxel"
version = "1.0.0"

val modularitySourceSet = sourceSets.register("modularity") {
    java {
        srcDirs(sourceSets.main.map { it.java.srcDirs })
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    maven("https://maven.minecraftforge.net")
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.modlauncher4)

    "modularityCompileOnly"(libs.jetbrains.annotations)
    "modularityCompileOnly"(libs.modlauncher9)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.modlauncher4)
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(8)
    }

    "compileModularityJava"(JavaCompile::class) {
        options.release.set(16)
    }

    javadoc {
        source = modularitySourceSet.get().allJava
        classpath = modularitySourceSet.get().compileClasspath
    }

    jar {
        from("LICENSE")
        from(modularitySourceSet.map { it.output }) {
            include("module-info.class")
        }
    }

    "sourcesJar"(Jar::class) {
        from("LICENSE")
        from(modularitySourceSet.map { it.allSource }) {
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

            pom {
                name.set("Unprotect")
                description.set("A ModLauncher plugin that makes protected and package-private code public.")
                url.set("https://github.com/Juuxel/Unprotect")

                licenses {
                    license {
                        name.set("Mozilla Public License Version 2.0")
                        url.set("https://www.mozilla.org/en-US/MPL/2.0/")
                    }
                }

                developers {
                    developer {
                        id.set("Juuxel")
                        name.set("Juuxel")
                        email.set("juuzsmods@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/Juuxel/Unprotect.git")
                    developerConnection.set("scm:git:ssh://github.com/Juuxel/Unprotect.git")
                    url.set("https://github.com/Juuxel/Unprotect")
                }
            }
        }

        repositories {
            maven {
                name = "ossrh"
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials(PasswordCredentials::class)
            }
        }
    }
}

if (project.hasProperty("signing.keyId")) {
    signing {
        sign(publishing.publications)
    }
}

spotless {
    java {
        licenseHeaderFile("HEADER.txt")
    }
}
