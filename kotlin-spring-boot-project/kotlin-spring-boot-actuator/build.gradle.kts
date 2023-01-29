
dependencies {
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-core"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-beans"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-context"))
    implementation("com.google.guava:guava:$guavaVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("javax.annotation:javax.annotation-api:$javaxAnnotationVersion")
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-jcl"))

    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot"))



    // CompileOnly Optional
    compileOnly("io.micrometer:micrometer-core:1.9.5")  // metrics
    compileOnly(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    compileOnly("io.netty:netty-codec-http:$nettyVersion")
    compileOnly("org.aspectj:aspectjweaver:$aspectJVersion")


    testImplementation(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    testImplementation("org.aspectj:aspectjweaver:$aspectJVersion")
}