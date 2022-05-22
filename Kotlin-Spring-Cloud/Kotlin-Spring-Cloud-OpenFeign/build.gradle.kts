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
    testApi(project(":Kotlin-Spring-Cloud:Kotlin-Spring-Cloud-Nacos:Kotlin-Spring-Cloud-Nacos-Discovery"))

    implementation(project(":Kotlin-Spring-Framework"))
    implementation(project(":Kotlin-Spring-Boot"))
    implementation(project(":Kotlin-Spring-Web"))
    implementation(project(":Kotlin-Spring-Boot-Autoconfigure"))
    implementation(project(":Kotlin-Spring-Cloud:Kotlin-Spring-Cloud-Common"))
    implementation(project(":Kotlin-Spring-Cloud:Kotlin-Spring-Cloud-Context"))
    implementation(project(":Kotlin-Spring-Cloud:Kotlin-Spring-Cloud-Ribbon"))

    implementation("org.springframework:spring-core:$springCoreVersion")
    implementation("com.netflix.ribbon:ribbon-loadbalancer:$ribbonVersion")
    implementation("com.netflix.ribbon:ribbon-core:$ribbonVersion")
    implementation("org.apache.httpcomponents:httpclient:$apacheHttpClientVersion")
    implementation("io.github.openfeign:feign-httpclient:$feignHttpClientVersion")
    implementation("io.github.openfeign:feign-core:$feignVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}