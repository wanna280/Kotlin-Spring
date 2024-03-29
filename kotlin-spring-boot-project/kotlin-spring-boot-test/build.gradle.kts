dependencies {
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-core"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-beans"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-context"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot-autoconfigure"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-test"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-jcl"))

    implementation("junit:junit:4.12")
    implementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    implementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")


    testImplementation(project(":kotlin-logger-project:kotlin-logger-slf4j-impl"))
}
