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
    implementation("org.aspectj:aspectjweaver:1.9.9")
    implementation("com.google.guava:guava:31.1-jre")
    implementation(kotlin("stdlib"))
    implementation("io.netty:netty-all:4.1.77.Final")
    implementation("org.springframework:spring-core:5.3.17")
    implementation(project(":Kotlin-Spring-Boot"))
    implementation(project(":Kotlin-Spring-Framework"))
    implementation(project(":Kotlin-Spring-Web"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}