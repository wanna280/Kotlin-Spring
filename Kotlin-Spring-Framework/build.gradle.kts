repositories.flatDir {
    dirs("libs")
}

dependencies {
    implementation("org.springframework:spring-core:$springCoreVersion")
    compileOnly(project(":Kotlin-Spring-Instrument"))  // compileOnly
    compileOnly("org.aspectj:aspectjweaver:$aspectJVersion")
    implementation(project(":Kotlin-Logger:logger-slf4j-impl"))
    implementation("cglib:cglib:$cglibVersion")
    testImplementation("com.alibaba:druid:1.2.10")
    testImplementation("mysql:mysql-connector-java:8.0.29")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}