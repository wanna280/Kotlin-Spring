dependencies {
    implementation("org.apache.commons:commons-compress:$commonCompressVersion")
}

// 生成的loader的jar包的存放位置
val generatedResources = "$buildDir/generated-resources/main"

// loader的项目路径
val loaderProjectPath = ":kotlin-spring-boot-project:kotlin-spring-boot-tools:kotlin-spring-boot-loader"

// 将spring-boot-loader工程去进行打包成为Jar, 并放入到当前Jar包的"META-INF/loader"下
tasks.create<Jar>("reproducibleLoaderJar") {
    // 获取到Loader工程的Jar任务
    val loaderJarTask = tasks.getByPath("$loaderProjectPath:jar") as Jar

    // 当前任务依赖于Loader工程的Jar任务, 等它执行完成之后, 这个任务再去进行构建
    this.dependsOn(loaderJarTask)

    // 在执行当前工程的Jar任务之前, 先去执行当前任务, 因为生成Jar包需要用到当前的任务
    tasks.getByName<Jar>("jar").dependsOn(this)

    // zipTree方法, 用于将归档文件去进行展开...
    // 将loader的工程的产物, 全部copy过来, 去生成一个新的Archive产物
    from(zipTree(loaderJarTask.archiveFile.get())) {
        include("**/**")
    }

    // 要去进行构建的Archive归档文件的文件名为"kotlin-spring-boot-loader.jar"
    this.archiveFileName.set("kotlin-spring-boot-loader.jar")

    // 要将构建出来的Archive归档文件放到"{generatedResource}/META-INF/loader"下, 这样最终就会将这个Jar包放到到META-INF/loader/目录下
    destinationDirectory.set(file("$generatedResources/META-INF/loader"))
}

sourceSets {
    // 把generatedResources去添加到main的SourceSet当中, 这样打包的时候, 就能自动将该Jar包去进行copy
    getByName("main") {
        this.output.dir(generatedResources)
    }
}