dependencies {
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-framework"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-jcl"))

    // compileOnly
    compileOnly(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    compileOnly("javax.servlet:javax.servlet-api:$servletApiVersion") // servlet-api
    compileOnly("org.apache.tomcat.embed:tomcat-embed-core:$tomcatCoreVersion")
    compileOnly("ch.qos.logback:logback-classic:$logbackVersion")  // logback


    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    compileOnly("io.netty:netty-codec-http:$nettyVersion")
}
