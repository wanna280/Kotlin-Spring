dependencies {

    // for Runtime
    implementation("cglib:cglib:$cglibVersion")

    // for Compile
    compileOnly(project(":kotlin-spring-framework-project:kotlin-spring-instrument"))  // compileOnly
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-jcl"))
    compileOnly("org.aspectj:aspectjweaver:$aspectJVersion")


    // for Test
    testImplementation(project(":kotlin-logger:logger-slf4j-impl"))
    testImplementation("com.alibaba:druid:$druidVersion")
    testImplementation("mysql:mysql-connector-java:$mysqlVersion")
}
