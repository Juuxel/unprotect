plugins {
    java
    `maven-publish`
    signing
    id("com.diffplug.spotless") version "6.5.1"
}

group = "io.github.juuxel"
version = "1.3.1"

val modularityJavaVersion = 16
val modularitySourceSet = sourceSets.register("modularity") {
    java {
        srcDirs(sourceSets.main.map { it.java.srcDirs })
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
    implementation(libs.log4j.api)

    "modularityCompileOnly"(libs.jetbrains.annotations)
    "modularityCompileOnly"(libs.modlauncher9)
    "modularityImplementation"(libs.log4j.api)
    "modularityCompileOnly"(fakeFancyModLoaderJar.map { it.outputs.files })

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.modlauncher4)
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(8)
    }

    "compileModularityJava"(JavaCompile::class) {
        options.release.set(modularityJavaVersion)
        options.compilerArgs.addAll(listOf("--module-version", project.version.toString()))
    }

    javadoc {
        source = modularitySourceSet.get().allJava
        classpath = modularitySourceSet.get().compileClasspath
    }

    jar {
        from("LICENSE")
        from(modularitySourceSet.map { it.output }) {
            include("module-info.class")
            into("META-INF/versions/$modularityJavaVersion")
        }
        manifest {
            attributes(
                "Multi-Release" to "true",
                "Premain-Class" to "juuxel.unprotect.UnprotectAgent",
            )
        }
    }

    "sourcesJar"(Jar::class) {
        from("LICENSE")
        from(modularitySourceSet.map { it.allSource }) {
            include("module-info.java")
            into("META-INF/versions/$modularityJavaVersion")
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
    }
}

if (System.getenv("MAVEN_CENTRAL_USERNAME") != null) {
    val centralUsername = System.getenv("MAVEN_CENTRAL_USERNAME")
    val centralPassword = System.getenv("MAVEN_CENTRAL_PASSWORD")

    publishing {
        repositories {
            maven {
                name = "ossrh-staging-api"
                url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                credentials {
                    username = centralUsername
                    password = centralPassword
                }
            }
        }
    }

    val uploadTask = tasks.register<juuxel.unprotect.gradle.UploadDefaultCentralRepository>("uploadDefaultCentralRepository") {
        dependsOn("publishMavenPublicationToOssrh-staging-apiRepository")
        username.set(centralUsername)
        password.set(centralPassword)
    }

    tasks.named("publish") {
        dependsOn(uploadTask)
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
