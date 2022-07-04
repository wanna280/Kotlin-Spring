dependencies {

    // for Runtime
    implementation("cglib:cglib:$cglibVersion")

    // for Compile
    compileOnly(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Instrument"))  // compileOnly
    compileOnly("org.aspectj:aspectjweaver:$aspectJVersion")


    // for Test
    testImplementation(project(":Kotlin-Logger:logger-slf4j-impl"))
    testImplementation("com.alibaba:druid:1.2.10")
    testImplementation("mysql:mysql-connector-java:8.0.29")
}
