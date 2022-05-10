plugins {
    kotlin("jvm") version "1.6.10"
    java
    id("io.spring.dependency-management") version("1.0.9.RELEASE")
}

group = "com.wanna"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    google()
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        google()
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}