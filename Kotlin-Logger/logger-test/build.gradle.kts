
dependencies {
    implementation(project(":Kotlin-Logger:logger-slf4j-impl"))
    implementation("org.slf4j:slf4j-api:$slf4jApiVersion")
    implementation("org.fusesource.jansi:jansi:$jansiVersion")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}