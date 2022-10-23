dependencies {

    compileOnly(project(":Kotlin-Spring-Cloud-Project:Kotlin-Spring-Cloud-Ribbon"))

    implementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Framework"))
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot"))
    implementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Web"))
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot-Autoconfigure"))
    implementation(project(":Kotlin-Spring-Cloud-Project:Kotlin-Spring-Cloud-Common"))
    implementation(project(":Kotlin-Spring-Cloud-Project:Kotlin-Spring-Cloud-Context"))


    implementation("com.netflix.ribbon:ribbon-loadbalancer:$ribbonVersion")
    implementation("com.netflix.ribbon:ribbon-core:$ribbonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson
    implementation("org.apache.httpcomponents:httpclient:$apacheHttpClientVersion")
    implementation("io.github.openfeign:feign-httpclient:$feignHttpClientVersion")
    implementation("io.github.openfeign:feign-core:$feignVersion")
    testImplementation("io.netty:netty-codec-http:$nettyVersion")

    testImplementation(project(":Kotlin-Spring-Cloud-Project:Kotlin-Spring-Cloud-Ribbon"))
    testImplementation(project(":Kotlin-Spring-Cloud-Project:Kotlin-Spring-Cloud-Nacos:Kotlin-Spring-Cloud-Nacos-Discovery"))
}