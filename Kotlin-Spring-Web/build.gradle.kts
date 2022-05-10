plugins {
    kotlin("jvm")
    java
}

group = "com.wanna"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")  // jackson
    implementation(project(":Kotlin-Logger:logger-slf4j-impl"))
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.springframework:spring-core:5.3.17")
    implementation("io.netty:netty-all:4.1.77.Final")
    implementation(project(":Kotlin-Spring-Framework"))
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}