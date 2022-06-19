dependencies {
    implementation(project(":Kotlin-Spring-Framework"))
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson
    compileOnly("io.netty:netty-codec-http:$nettyVersion")
    implementation("org.apache.httpcomponents:httpclient:$apacheHttpClientVersion")

    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    testImplementation(project(":Kotlin-Logger:logger-slf4j-impl"))
    testImplementation("io.netty:netty-codec-http:$nettyVersion")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}