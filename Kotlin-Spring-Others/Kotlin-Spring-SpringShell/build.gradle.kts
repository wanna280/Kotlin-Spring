

dependencies {
    implementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Framework"))
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot"))
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot-Autoconfigure"))


    testImplementation(project(":Kotlin-Logger:logger-slf4j-impl"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}