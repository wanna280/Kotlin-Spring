

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
    implementation("com.alibaba.nacos:nacos-client:$nacosClientVersion")
}