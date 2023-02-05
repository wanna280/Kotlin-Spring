dependencies {
    implementation("org.apache.commons:commons-compress:$commonCompressVersion")
}

val generatedResources = "$buildDir/generated-resources/main"



// 将spring-boot-loader去打包一下(目前做法不太行, 待优化)
tasks.create<Jar>("reproducibleLoaderJar") {
    val loaderProject = project(":kotlin-spring-boot-project:kotlin-spring-boot-tools:kotlin-spring-boot-loader")

    // zipFile, 用于将归档文件去进行展开...
    from(zipTree(loaderProject.buildDir.absolutePath + "/libs/kotlin-spring-boot-loader-${project.version}.jar")) {
        include("**/**")
    }

    tasks.getByName<Jar>("jar").dependsOn(this)

    // 要去进行构建的Archive归档文件的文件名?
    this.archiveFileName.set("kotlin-spring-boot-loader.jar")

    // 要将构建出来的Archive归档文件放到哪里?
    destinationDirectory.set(file("$generatedResources/META-INF/loader"))
}

sourceSets {
    getByName("main") {
        this.output.dir(generatedResources)
    }
}