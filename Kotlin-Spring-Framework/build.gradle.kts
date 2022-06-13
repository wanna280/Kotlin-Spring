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
    compileOnly("javax.inject:javax.inject:1")
    compileOnly(project(":Kotlin-Spring-Instrument"))  // compileOnly
    compileOnly("org.aspectj:aspectjweaver:$aspectJVersion")
    implementation(project(":Kotlin-Logger:logger-slf4j-impl"))
    implementation("org.slf4j:slf4j-api:$slf4jApiVersion")
    implementation("org.springframework:spring-core:$springCoreVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("cglib:cglib:$cglibVersion")
    implementation("javax.annotation:javax.annotation-api:$javaxAnnotationVersion")

    testImplementation("com.alibaba:druid:1.2.10")
    testImplementation("mysql:mysql-connector-java:8.0.29")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}