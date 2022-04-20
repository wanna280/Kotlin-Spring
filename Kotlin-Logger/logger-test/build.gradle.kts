plugins {
    kotlin("jvm")
    java
}

group = "com.wanna"
version = "1.0-SNAPSHOT"

repositories.flatDir {
    dirs("libs")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.wanna:logger-api:1.0-SNAPSHOT")
    implementation("com.wanna:logger-impl:1.0-SNAPSHOT")
    implementation("org.fusesource.jansi:jansi:2.4.0")
    implementation(kotlin("stdlib"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}