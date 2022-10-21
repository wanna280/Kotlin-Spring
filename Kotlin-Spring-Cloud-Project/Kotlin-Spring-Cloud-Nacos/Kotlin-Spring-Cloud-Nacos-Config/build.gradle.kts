
dependencies {
    implementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Framework"))
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot"))
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot-Autoconfigure"))
    implementation(project(":Kotlin-Logger:logger-slf4j-impl"))
    implementation(project(":Kotlin-Spring-Cloud-Project:Kotlin-Spring-Cloud-Context"))
    implementation(project(":Kotlin-Spring-Cloud-Project:Kotlin-Spring-Cloud-Common"))
    implementation("com.alibaba.nacos:nacos-client:$nacosClientVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson

    // 添加这些依赖，在test时才能正常启动
    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    testImplementation(project(":Kotlin-Spring-Cloud-Project:Kotlin-Spring-Cloud-Ribbon"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}