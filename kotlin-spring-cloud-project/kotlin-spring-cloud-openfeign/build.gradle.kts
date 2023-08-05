dependencies {
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-core"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-beans"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-context"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-jcl"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot-autoconfigure"))
    implementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-common"))
    implementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-context"))


    implementation("com.netflix.ribbon:ribbon-loadbalancer:$ribbonVersion")
    implementation("com.netflix.ribbon:ribbon-core:$ribbonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson
    implementation("org.slf4j:slf4j-api:$slf4jApiVersion")
    implementation("io.github.openfeign:feign-httpclient:$feignHttpClientVersion")
    implementation("io.github.openfeign:feign-core:$feignVersion")

    // compileOnly
    compileOnly(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-ribbon"))
    compileOnly("org.apache.httpcomponents:httpclient:$apacheHttpClientVersion")

    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    testImplementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-ribbon"))
    testImplementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-nacos:kotlin-spring-cloud-nacos-discovery"))
}