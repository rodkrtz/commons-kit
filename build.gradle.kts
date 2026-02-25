plugins {
    kotlin("jvm") version "2.3.0"
}

group = "com.rodkrtz"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
    explicitApi()
}

tasks.test {
    useJUnitPlatform()
}