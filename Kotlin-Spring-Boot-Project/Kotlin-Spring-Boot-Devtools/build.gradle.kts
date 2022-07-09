dependencies {
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot"))
    implementation(project(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot-Autoconfigure"))
    implementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Framework"))

    // compileOnly
    compileOnly(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Web"))
}
