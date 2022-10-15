dependencies {
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot"))
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot-Autoconfigure"))
    implementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Framework"))
    implementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Web"))

    testImplementation("org.apache.httpcomponents:httpclient:$apacheHttpClientVersion")
    testImplementation(project(":Kotlin-Logger:logger-slf4j-impl"))
}
