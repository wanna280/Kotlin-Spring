dependencies {

    implementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Framework"))

    implementation("junit:junit:4.12")
    implementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    implementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")


    // for Test
    testImplementation(project(":Kotlin-Logger:logger-slf4j-impl"))
}
