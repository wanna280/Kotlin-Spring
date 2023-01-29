dependencies {
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-core"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-beans"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-context"))
    implementation("com.netflix.ribbon:ribbon-loadbalancer:$ribbonVersion")
    implementation("com.netflix.ribbon:ribbon-core:$ribbonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")  // jackson
    implementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-context"))
    implementation(project(":kotlin-spring-cloud-project:kotlin-spring-cloud-common"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-context"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-jcl"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot-autoconfigure"))
    implementation(project(":kotlin-logger-project:kotlin-logger-slf4j-impl"))
}