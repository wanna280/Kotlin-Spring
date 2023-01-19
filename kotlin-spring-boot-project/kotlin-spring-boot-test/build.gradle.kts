dependencies {
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot-autoconfigure"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-framework"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-test"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-jcl"))

    implementation("junit:junit:4.12")
    implementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    implementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")


    testImplementation(project(":kotlin-logger:logger-slf4j-impl"))
}
