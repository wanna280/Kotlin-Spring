plugins {
    kotlin("jvm")
    java
}

group = "com.wanna"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("org.fusesource.jansi:jansi:2.4.0")
    implementation(project(":Kotlin-Logger:logger-api"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}