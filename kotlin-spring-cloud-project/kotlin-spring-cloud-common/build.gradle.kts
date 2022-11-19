

dependencies {
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-framework"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot-autoconfigure"))

    // for Test
    testImplementation(project(":kotlin-logger:logger-slf4j-impl"))
}