plugins {
    kotlin("jvm")
    java
}

group = "com.wanna"
version = "1.0-SNAPSHOT"


dependencies {
    implementation("com.netflix.ribbon:ribbon-loadbalancer:$ribbonVersion")
    implementation("com.netflix.ribbon:ribbon-core:$ribbonVersion")
    implementation(project(":Kotlin-Spring-Cloud:Kotlin-Spring-Cloud-Context"))
    implementation(project(":Kotlin-Spring-Cloud:Kotlin-Spring-Cloud-Common"))
    implementation(project(":Kotlin-Spring-Framework"))
    implementation(project(":Kotlin-Spring-Boot"))
    implementation(project(":Kotlin-Spring-Boot-Autoconfigure"))
    implementation(project(":Kotlin-Logger:logger-slf4j-impl"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}