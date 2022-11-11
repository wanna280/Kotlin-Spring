

dependencies {
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-framework"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot-autoconfigure"))


    testImplementation(project(":kotlin-logger:logger-slf4j-impl"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}