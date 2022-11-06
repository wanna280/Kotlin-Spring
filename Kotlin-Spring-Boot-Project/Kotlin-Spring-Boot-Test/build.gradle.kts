dependencies {
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot"))
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot-Autoconfigure"))
    implementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Framework"))
    implementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Web"))
    implementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Test"))

    implementation("junit:junit:4.12")
    implementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    implementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")


    testImplementation(project(":Kotlin-Logger:logger-slf4j-impl"))
}
