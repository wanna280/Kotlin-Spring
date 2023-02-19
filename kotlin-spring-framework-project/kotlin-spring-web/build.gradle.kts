dependencies {
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-core"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-beans"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-aop"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-context"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-jcl"))

    // compileOnly
    compileOnly("org.apache.httpcomponents:httpclient:$apacheHttpClientVersion")  // apache httpcomponents
    compileOnly("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson
    compileOnly("com.google.code.gson:gson:$gsonVersion")  // gson
    compileOnly("javax.servlet:javax.servlet-api:$servletApiVersion") // servlet-api


    // for test
    testImplementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")
}