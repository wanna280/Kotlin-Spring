plugins {
    id("com.github.johnrengelman.shadow") version ("6.1.0")
}

// ShadowJar这个任务, 默认会将所有的Jar包都解压, 得到最终的一个大的Jar包(archiveFileName=Kotlin-Spring-Boot-Loader-1.0-SNAPSHOT-all.jar)
val shadowJar = tasks.getByName<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    println("执行ShadowJar任务的目标文件为 ${archiveFileName.get()}")
}


// 自定义一个ShadowJar任务
tasks.register<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("kotlinStdlibRepackJar") {
    // 生成的目标文件名
    archiveFileName.set("Spring-Kotlin-Stdlib-Repack-$kotlinVersion.jar")

    // Note: 只有设置Configuration才能输出文件！！！！！！！！！！！不设置啥也没有
    this.configurations = shadowJar.configurations
    shadowJar.configurations
        .filter { it.name == "runtimeClasspath" }  // 获取runtimeClasspath的配置信息
        .forEach {
            it.files.forEach {
                // 获取runtimeClassPath的文件列表
            }
        }
    // relocate用于去进行重定向, 将某个目录重定向到另外一个目录
    // relocate("kotlin", "com.wanna.boot.loader.kotlin")
}

tasks.getByName<org.gradle.jvm.tasks.Jar>("jar") {
    manifest.attributes["Implementation-Title"] = project.name
    manifest.attributes["Main-Class"] = "com.wanna.boot.loader.JarLauncher"

    // for debug
    manifest.attributes["Start-Class"] = "com.wanna.boot.loader.debug.DebugApp"

    // 获取到ShadowJarTask
    val shadowJar = tasks.getByName<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("kotlinStdlibRepackJar")

    // 让jar任务依赖ShadowJarTask, 在构建Jar的时候, 把ShadowJar也一起执行
    dependsOn(shadowJar)

    // 将ShadowJar任务输出的构建文件当中的"kotlin/"开头的文件全部拷贝到当前的Jar包当中...
    from(zipTree(shadowJar.archiveFile.get())) {
        include("kotlin/**")
    }
}