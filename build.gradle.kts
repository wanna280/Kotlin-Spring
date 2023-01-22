plugins {
    kotlin("jvm") version kotlinVersion
    java
    maven
}

allprojects {
    group = "com.wanna"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        jcenter()
        google()
        mavenLocal()
    }

    apply {
        plugin("java")
        plugin("maven")
        plugin("maven-publish")
        plugin("kotlin")
        plugin(com.wanna.plugin.BuildPlugin::class.java)
    }

    dependencies {
        implementation("org.springframework:spring-core:$springCoreVersion") {
            // 不需要jcl依赖, 但是因为我们用到了Spring的一些工具类, 因此没有这个依赖的话, 会出现链接错误
//            exclude(group = "org.springframework", module = "spring-jcl")
        }

        implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
        implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
        compileOnly("javax.validation:validation-api:$javaxValidationVersion")  // for  validate
        compileOnly("javax.inject:javax.inject:$javaxInjectVersion")
        compileOnly("com.google.code.findbugs:jsr305:$googleJsr305Version")
        implementation("javax.annotation:javax.annotation-api:$javaxAnnotationVersion")
    }

    // Note: AllProjects会在父工程当中执行, 它无法获取到子工程的插件当中的Task, 因为执行当前脚本时, 子工程的脚本还没执行

    tasks.getByName<org.gradle.jvm.tasks.Jar>("jar") {
        manifest.attributes["Implementation-Title"] = project.name
        manifest.attributes["Implementation-Version"] = rootProject.version
        manifest.attributes["Created-By"] =
            "${System.getProperty("java.version")} (${System.getProperty("java.specification.vendor")})"

        // Note: KotlinSourcesJar是org.gradle.jvm.tasks.Jar, 而不是它的子类org.gradle.api.tasks.bundling.Jar
        // dependsOn, 设置jar任务的同时也去执行KotlinSourcesJar任务去进行构建
        dependsOn(tasks.getByName("kotlinSourcesJar"))
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
        kotlinOptions.jvmTarget = "1.8"  // set JvmTarget=1.8
    }

    // 新增产物的构件, 添加KotlinSourcesJar任务, 将源码jar包也一起去进行构建到maven仓库
    artifacts {
        archives(tasks.getByName("kotlinSourcesJar"))
    }
}