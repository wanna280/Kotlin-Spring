plugins {
    kotlin("jvm")
    java
}

group = "com.wanna"
version = "1.0-SNAPSHOT"

repositories.flatDir {
    dirs("libs")
}

dependencies {
    implementation(project(":Kotlin-Logger:logger-slf4j-impl"))
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.fusesource.jansi:jansi:2.4.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}