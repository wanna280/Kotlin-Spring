dependencies {
    implementation("org.apache.commons:commons-compress:$commonCompressVersion")
}


tasks.withType<Jar> {
    this.archiveFileName.set("kotlin-spring-boot-loader.jar")
    destinationDirectory.set(file("$buildDir/generated-resources/main/META-INF/loader"))
}