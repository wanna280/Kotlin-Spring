dependencies {
    implementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Framework"))
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot"))
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot-Autoconfigure"))

    implementation("org.mybatis:mybatis:$mybatisVersion")

    testImplementation("com.alibaba:druid:1.2.10")
    testImplementation("mysql:mysql-connector-java:8.0.29")
    testImplementation(project(":Kotlin-Logger:logger-slf4j-impl"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}