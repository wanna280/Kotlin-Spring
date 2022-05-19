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
    implementation(project(":Kotlin-Spring-Cloud-Common"))
    implementation(project(":Kotlin-Spring-Cloud-Context"))
    implementation(project(":Kotlin-Spring-Framework"))
    implementation(project(":Kotlin-Spring-Boot"))
    implementation(project(":Kotlin-Spring-Boot-Autoconfigure"))
    implementation(project(":Kotlin-Logger:logger-slf4j-impl"))
    implementation("javax.annotation:javax.annotation-api:$javaxAnnotationVersion")
    implementation("com.alibaba.nacos:nacos-client:$nacosClientVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}