dependencies {
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-core"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-beans"))
    compileOnly(project(":kotlin-spring-framework-project:kotlin-spring-aop"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-context"))
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-jcl"))
    implementation("org.slf4j:slf4j-api:$slf4jApiVersion")
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-web"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot"))
    implementation(project(":kotlin-spring-boot-project:kotlin-spring-boot-autoconfigure"))
    implementation("org.slf4j:slf4j-api:$slf4jApiVersion")
}
