plugins {
    id("io.github.juuxel.unprotect")
}

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":"))
    compileOnly(libs.fancymodloader)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile> {
    options.release = 21
    options.javaModuleVersion = project.version.toString()
}
