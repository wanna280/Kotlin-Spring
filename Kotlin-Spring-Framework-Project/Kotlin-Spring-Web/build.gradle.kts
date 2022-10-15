dependencies {
    implementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Framework"))

    // compileOnly
    compileOnly("org.apache.httpcomponents:httpclient:$apacheHttpClientVersion")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson
    compileOnly("io.netty:netty-codec-http:$nettyVersion")

    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // for test
    testImplementation(project(":Kotlin-Logger:logger-slf4j-impl"))
    testImplementation("io.netty:netty-codec-http:$nettyVersion")
}