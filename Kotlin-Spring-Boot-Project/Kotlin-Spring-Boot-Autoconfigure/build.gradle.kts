
dependencies {
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-framework"))

    // CompileOnly Optional
    compileOnly(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    compileOnly("io.netty:netty-codec-http:$nettyVersion")
    compileOnly("org.aspectj:aspectjweaver:$aspectJVersion")


    implementation("com.google.guava:guava:$guavaVersion")


    testImplementation(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    testImplementation("org.aspectj:aspectjweaver:$aspectJVersion")
}