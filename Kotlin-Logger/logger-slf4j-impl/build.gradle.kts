plugins {
    kotlin("jvm")
    java
}

group = "com.wanna"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:$slf4jApiVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation(project(":Kotlin-Logger:logger-impl"))
    compileOnly(project(":Kotlin-Logger:logger-api"))
}
