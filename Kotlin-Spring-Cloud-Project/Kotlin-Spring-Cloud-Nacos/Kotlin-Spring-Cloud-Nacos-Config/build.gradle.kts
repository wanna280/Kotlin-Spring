
dependencies {
    implementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Framework"))
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot"))
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot-Autoconfigure"))
    implementation(project(":Kotlin-Logger:logger-slf4j-impl"))
    implementation(project(":Kotlin-Spring-Cloud-Project:Kotlin-Spring-Cloud-Context"))
    implementation(project(":Kotlin-Spring-Cloud-Project:Kotlin-Spring-Cloud-Common"))

    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    implementation("com.alibaba.nacos:nacos-client:$nacosClientVersion")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}