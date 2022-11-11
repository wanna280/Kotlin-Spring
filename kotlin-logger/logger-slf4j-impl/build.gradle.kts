dependencies {
    implementation("org.slf4j:slf4j-api:$slf4jApiVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation(project(":kotlin-logger:logger-impl"))
    compileOnly(project(":kotlin-logger:logger-api"))
}
