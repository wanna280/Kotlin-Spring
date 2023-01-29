dependencies {
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-core"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-beans"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-context"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-jcl"))

    // CompileOnly Optional
    compileOnly(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    compileOnly("io.netty:netty-codec-http:$nettyVersion")
    compileOnly("org.aspectj:aspectjweaver:$aspectJVersion")
    compileOnly("javax.servlet:javax.servlet-api:$servletApiVersion") // servlet-api
    compileOnly("org.apache.tomcat.embed:tomcat-embed-core:$tomcatCoreVersion")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson
    compileOnly("com.google.code.gson:gson:$gsonVersion")  // gson


    implementation("com.google.guava:guava:$guavaVersion")

    testImplementation(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    testImplementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson
    testImplementation("org.apache.tomcat.embed:tomcat-embed-core:$tomcatCoreVersion")
    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    testImplementation("org.aspectj:aspectjweaver:$aspectJVersion")
    testImplementation(project(":kotlin-logger:logger-slf4j-impl"))
}