

dependencies {
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-core"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-beans"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-context"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-jcl"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot-autoconfigure"))

    // for Test
    testImplementation(project(":kotlin-logger-project:kotlin-logger-slf4j-impl"))
}