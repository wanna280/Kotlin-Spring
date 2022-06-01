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
    implementation(project(":Kotlin-Spring-Framework"))
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson
    implementation(project(":Kotlin-Logger:logger-slf4j-impl"))
    implementation("org.slf4j:slf4j-api:$slf4jApiVersion")
    implementation("org.springframework:spring-core:$springCoreVersion")
    compileOnly("io.netty:netty-codec-http:$nettyVersion")
    implementation("org.apache.httpcomponents:httpclient:$apacheHttpClientVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}