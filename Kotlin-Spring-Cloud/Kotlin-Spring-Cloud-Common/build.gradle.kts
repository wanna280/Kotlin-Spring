plugins {
    kotlin("jvm")
    java
}

group = "com.wanna"
version = "1.0-SNAPSHOT"


dependencies {
    implementation(project(":Kotlin-Spring-Web"))
    implementation(project(":Kotlin-Spring-Framework"))
    implementation(project(":Kotlin-Spring-Boot"))
    implementation(project(":Kotlin-Spring-Boot-Autoconfigure"))
    implementation(project(":Kotlin-Logger:logger-slf4j-impl"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}