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
    testImplementation(project(":Kotlin-Spring-Web"))
    testImplementation("io.netty:netty-codec-http:$nettyVersion")


    implementation("com.netflix.ribbon:ribbon-loadbalancer:$ribbonVersion")
    implementation("com.netflix.ribbon:ribbon-core:$ribbonVersion")
    implementation(project(":Kotlin-Spring-Cloud:Kotlin-Spring-Cloud-Ribbon"))
    implementation(project(":Kotlin-Spring-Cloud:Kotlin-Spring-Cloud-Context"))
    implementation(project(":Kotlin-Spring-Cloud:Kotlin-Spring-Cloud-Common"))
    implementation(project(":Kotlin-Spring-Framework"))
    implementation(project(":Kotlin-Spring-Boot"))
    implementation(project(":Kotlin-Spring-Boot-Autoconfigure"))
    implementation(project(":Kotlin-Logger:logger-slf4j-impl"))
    implementation("javax.annotation:javax.annotation-api:$javaxAnnotationVersion")
    implementation("com.alibaba.nacos:nacos-client:$nacosClientVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}