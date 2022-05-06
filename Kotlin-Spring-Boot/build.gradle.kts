plugins {
    kotlin("jvm")
    java
}

group = "com.wanna"
version = "1.0-SNAPSHOT"


dependencies {
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.springframework:spring-core:5.3.17")
    implementation(project(":Kotlin-Spring-Framework"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}