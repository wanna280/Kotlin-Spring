plugins {
    kotlin("jvm") version kotlinVersion
    java
    id("io.spring.dependency-management") version ("1.0.9.RELEASE")
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