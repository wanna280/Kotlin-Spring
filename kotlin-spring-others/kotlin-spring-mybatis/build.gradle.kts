dependencies {
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-framework"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot-autoconfigure"))

    implementation("org.mybatis:mybatis:$mybatisVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson

    testImplementation("com.alibaba:druid:1.2.10")
    testImplementation("mysql:mysql-connector-java:8.0.29")
    testImplementation(project(":kotlin-logger:logger-slf4j-impl"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}