
dependencies {
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-core"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-beans"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-context"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot-autoconfigure"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-context"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot-actuator"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-jcl"))


    // CompileOnly Optional
    compileOnly(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    compileOnly("io.micrometer:micrometer-core:1.9.5")  // metrics
    compileOnly("io.netty:netty-codec-http:$nettyVersion")
    compileOnly("org.aspectj:aspectjweaver:$aspectJVersion")


    implementation("com.google.guava:guava:$guavaVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("javax.annotation:javax.annotation-api:$javaxAnnotationVersion")


    testImplementation(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    testImplementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")
    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    testImplementation("org.aspectj:aspectjweaver:$aspectJVersion")
    testImplementation(project(":kotlin-logger-project:kotlin-logger-slf4j-impl"))
}