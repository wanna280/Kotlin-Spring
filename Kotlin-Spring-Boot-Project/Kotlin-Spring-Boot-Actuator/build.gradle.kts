
dependencies {
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot"))
    implementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Framework"))

    // CompileOnly Optional
    compileOnly(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Web"))
    compileOnly("io.netty:netty-codec-http:$nettyVersion")
    compileOnly("org.aspectj:aspectjweaver:$aspectJVersion")


    implementation("com.google.guava:guava:$guavaVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.springframework:spring-core:$springCoreVersion")
    implementation("javax.annotation:javax.annotation-api:$javaxAnnotationVersion")


    testImplementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Web"))
    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    testImplementation("org.aspectj:aspectjweaver:$aspectJVersion")
}