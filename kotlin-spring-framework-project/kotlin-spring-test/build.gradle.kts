dependencies {

    implementation(project(":kotlin-spring-framework-project:kotlin-spring-framework"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-jcl"))

    compileOnly("junit:junit:4.12")
    compileOnly("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    compileOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")


    // for Test
    testImplementation(project(":kotlin-logger:logger-slf4j-impl"))
    testImplementation("junit:junit:4.12")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}
