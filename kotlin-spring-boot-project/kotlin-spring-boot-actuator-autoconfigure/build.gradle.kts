
dependencies {
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot-autoconfigure"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-framework"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot-actuator"))


    // CompileOnly Optional
    compileOnly(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    compileOnly("io.micrometer:micrometer-core:1.9.5")  // metrics
    compileOnly("io.netty:netty-codec-http:$nettyVersion")
    compileOnly("org.aspectj:aspectjweaver:$aspectJVersion")


    implementation("com.google.guava:guava:$guavaVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.springframework:spring-core:$springCoreVersion")
    implementation("javax.annotation:javax.annotation-api:$javaxAnnotationVersion")


    testImplementation(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    testImplementation("org.aspectj:aspectjweaver:$aspectJVersion")
    testImplementation(project(":kotlin-logger:logger-slf4j-impl"))
}