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
    compileOnly(project(":Kotlin-Spring-Instrument"))  // compileOnly
    compileOnly("org.aspectj:aspectjweaver:1.9.9")
    implementation(project(":Kotlin-Logger:logger-slf4j-impl"))
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.springframework:spring-core:5.3.17")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
    implementation("cglib:cglib:3.3.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}