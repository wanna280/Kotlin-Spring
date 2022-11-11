dependencies {
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-framework"))

    // compileOnly
    compileOnly("org.apache.httpcomponents:httpclient:$apacheHttpClientVersion")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson
    compileOnly("io.netty:netty-codec-http:$nettyVersion")

    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // for test
    testImplementation(project(":kotlin-logger:logger-slf4j-impl"))
    testImplementation("io.netty:netty-codec-http:$nettyVersion")
}