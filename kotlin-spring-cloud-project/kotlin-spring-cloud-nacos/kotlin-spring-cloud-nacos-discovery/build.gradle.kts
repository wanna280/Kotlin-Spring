dependencies {
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-core"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-beans"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-context"))
    implementation("com.netflix.ribbon:ribbon-loadbalancer:$ribbonVersion")
    implementation("com.netflix.ribbon:ribbon-core:$ribbonVersion")
    implementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-ribbon"))
    implementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-context"))
    implementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-common"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-context"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-jcl"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot-autoconfigure"))

    implementation("com.alibaba.nacos:nacos-client:$nacosClientVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson

    // for Test
    testImplementation(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    testImplementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-ribbon"))
    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    testImplementation(project(":kotlin-logger:logger-slf4j-impl"))
}