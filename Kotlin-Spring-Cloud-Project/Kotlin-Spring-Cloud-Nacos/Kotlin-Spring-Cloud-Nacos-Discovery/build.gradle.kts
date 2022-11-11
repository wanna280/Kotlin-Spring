dependencies {
    implementation("com.netflix.ribbon:ribbon-loadbalancer:$ribbonVersion")
    implementation("com.netflix.ribbon:ribbon-core:$ribbonVersion")
    implementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-ribbon"))
    implementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-context"))
    implementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-common"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-framework"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot-autoconfigure"))
    implementation(project(":kotlin-logger:logger-slf4j-impl"))
    implementation("com.alibaba.nacos:nacos-client:$nacosClientVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson

    // for Test
    testImplementation(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    testImplementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-ribbon"))
    testImplementation("io.netty:netty-codec-http:$nettyVersion")
}