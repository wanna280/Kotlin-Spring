dependencies {

    implementation(project(":kotlin-spring-framework-project:kotlin-spring-core"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-beans"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-context"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-jcl"))
    implementation("org.slf4j:slf4j-api:$slf4jApiVersion")

    // compileOnly
    compileOnly(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    compileOnly("org.apache.httpcomponents:httpclient:$apacheHttpClientVersion")  // apache httpcomponents
    compileOnly("javax.servlet:javax.servlet-api:$servletApiVersion") // servlet-api
    compileOnly("org.apache.tomcat.embed:tomcat-embed-core:$tomcatCoreVersion") // tomcat-core
    compileOnly("ch.qos.logback:logback-classic:$logbackVersion")  // logback


    testImplementation("io.netty:netty-codec-http:$nettyVersion")
    compileOnly("io.netty:netty-codec-http:$nettyVersion")
}
