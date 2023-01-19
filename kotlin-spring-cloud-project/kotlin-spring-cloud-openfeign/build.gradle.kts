dependencies {

    compileOnly(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-ribbon"))

    implementation(project(":kotlin-spring-framework-project:kotlin-spring-framework"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-jcl"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot-autoconfigure"))
    implementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-common"))
    implementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-context"))


    implementation("com.netflix.ribbon:ribbon-loadbalancer:$ribbonVersion")
    implementation("com.netflix.ribbon:ribbon-core:$ribbonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson
    implementation("org.apache.httpcomponents:httpclient:$apacheHttpClientVersion")
    implementation("io.github.openfeign:feign-httpclient:$feignHttpClientVersion")
    implementation("io.github.openfeign:feign-core:$feignVersion")
    testImplementation("io.netty:netty-codec-http:$nettyVersion")

    testImplementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-ribbon"))
    testImplementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-nacos:kotlin-spring-cloud-nacos-discovery"))
}