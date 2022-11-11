
dependencies {
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-framework"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot-autoconfigure"))
    implementation(project(":kotlin-logger:logger-slf4j-impl"))
    implementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-context"))
    implementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-common"))
    implementation("com.alibaba.nacos:nacos-client:$nacosClientVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson

    // 添加这些依赖，在test时才能正常启动
    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    testImplementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-ribbon"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}