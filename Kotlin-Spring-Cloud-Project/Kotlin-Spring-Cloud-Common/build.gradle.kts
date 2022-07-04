

dependencies {
    implementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Web"))
    implementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Framework"))
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot"))
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot-Autoconfigure"))

    // for Test
    testImplementation(project(":Kotlin-Logger:logger-slf4j-impl"))
}