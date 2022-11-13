

dependencies {
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot-autoconfigure"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-framework"))

    implementation(project(":kotlin-spring-others:kotlin-nacos:kotlin-nacos-api"))

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson
    implementation("org.slf4j:slf4j-api:$slf4jApiVersion")
    implementation("io.netty:netty-codec-http:$nettyVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    implementation(project(":kotlin-logger:logger-slf4j-impl"))

    testImplementation("org.apache.httpcomponents:httpclient:$apacheHttpClientVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}