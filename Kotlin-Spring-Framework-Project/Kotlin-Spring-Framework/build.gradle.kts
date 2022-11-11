dependencies {

    // for Runtime
    implementation("cglib:cglib:$cglibVersion")

    // for Compile
    compileOnly(project(":kotlin-spring-framework-project:kotlin-spring-instrument"))  // compileOnly
    compileOnly("org.aspectj:aspectjweaver:$aspectJVersion")


    // for Test
    testImplementation(project(":kotlin-logger:logger-slf4j-impl"))
    testImplementation("com.alibaba:druid:1.2.10")
    testImplementation("mysql:mysql-connector-java:8.0.29")
}
