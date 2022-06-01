plugins {
    kotlin("jvm")
    java
}

group = "com.wanna"
version = "1.0-SNAPSHOT"


dependencies {
    implementation(project(":Kotlin-Spring-Framework"))
    implementation(project(":Kotlin-Spring-Web"))
    implementation("org.slf4j:slf4j-api:$slf4jApiVersion")
    implementation("org.springframework:spring-core:$springCoreVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}