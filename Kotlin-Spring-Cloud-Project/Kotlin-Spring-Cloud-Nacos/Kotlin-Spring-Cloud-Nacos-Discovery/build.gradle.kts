dependencies {
    implementation("com.netflix.ribbon:ribbon-loadbalancer:$ribbonVersion")
    implementation("com.netflix.ribbon:ribbon-core:$ribbonVersion")
    implementation(project(":Kotlin-Spring-Cloud-Project:Kotlin-Spring-Cloud-Ribbon"))
    implementation(project(":Kotlin-Spring-Cloud-Project:Kotlin-Spring-Cloud-Context"))
    implementation(project(":Kotlin-Spring-Cloud-Project:Kotlin-Spring-Cloud-Common"))
    implementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Framework"))
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot"))
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot-Autoconfigure"))
    implementation(project(":Kotlin-Logger:logger-slf4j-impl"))
    implementation("com.alibaba.nacos:nacos-client:$nacosClientVersion")

    // for Test
    testImplementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Web"))
    testImplementation("io.netty:netty-codec-http:$nettyVersion")
}