import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version (shadowJarVersion)
}

// ShadowJar这个任务, 默认会将所有的Jar包都解压, 得到最终的一个大的Jar包(archiveFileName=kotlin-spring-boot-loader-1.0-SNAPSHOT-all.jar)
val shadowJar = tasks.getByName<ShadowJar>("shadowJar")

// 自定义一个ShadowJar任务
tasks.register<ShadowJar>("kotlinStdlibRepackJar") {
    // 生成的目标文件名
    archiveFileName.set("spring-kotlin-stdlib-repack-$kotlinVersion.jar")

    // Note: 只有设置Configuration才能输出文件!!!!!!不设置啥也没有
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

    // 获取到ShadowJarTask
    val shadowJar = tasks.getByName<ShadowJar>("kotlinStdlibRepackJar")

    // 让jar任务依赖ShadowJarTask, 在构建Jar的时候, 把ShadowJar也一起执行
    dependsOn(shadowJar)


    from(zipTree(shadowJar.archiveFile.get())) {
        // 将ShadowJar任务输出的构建文件当中的"kotlin/"开头的文件全部拷贝到当前的Jar包当中...
         include("kotlin/**")

        // exclude("kotlin/jvm/internal/impl/**")


//        include("kotlin/*.class")
//        include("kotlin/internal/**")
//        include("kotlin/collections/**")
//        include("kotlin/jvm/internal/**")
//        include("kotlin/jvm/internal/markers/*.class")
//        include("kotlin/ranges/*.class")
//        include("kotlin/text/*.class")
//        include("kotlin/reflect/*.class")
//        include("kotlin/io/**")
    }
}