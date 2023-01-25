dependencies {
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-framework"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-jcl"))

    compileOnly("junit:junit:$junit4Version")
    compileOnly("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    compileOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    // for test
    testImplementation(project(":kotlin-logger:logger-slf4j-impl"))
    testImplementation("junit:junit:$junit4Version")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}
