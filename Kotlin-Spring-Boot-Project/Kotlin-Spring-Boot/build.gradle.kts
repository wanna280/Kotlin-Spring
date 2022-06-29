dependencies {
    implementation(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Framework"))
    compileOnly(project(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Web"))
    implementation("org.slf4j:slf4j-api:$slf4jApiVersion")
    implementation("org.springframework:spring-core:$springCoreVersion")
}
