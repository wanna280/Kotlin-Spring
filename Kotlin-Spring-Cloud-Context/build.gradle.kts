plugins {
    kotlin("jvm")
    java
}

group = "com.wanna"
version = "1.0-SNAPSHOT"


dependencies {
    implementation(project(":Kotlin-Spring-Framework"))
    implementation(project(":Kotlin-Spring-Boot"))
    implementation(project(":Kotlin-Spring-Boot-Autoconfigure"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}