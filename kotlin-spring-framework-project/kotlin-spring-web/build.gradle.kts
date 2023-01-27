dependencies {
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-framework"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-jcl"))

    // compileOnly
    compileOnly("org.apache.httpcomponents:httpclient:$apacheHttpClientVersion")  // apache httpcomponents
    compileOnly("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson
    compileOnly("javax.servlet:javax.servlet-api:$servletApiVersion") // servlet-api


    testImplementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // for test
    testImplementation(project(":kotlin-logger:logger-slf4j-impl"))
}