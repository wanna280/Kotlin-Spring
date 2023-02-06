plugins {
    kotlin("jvm") version kotlinVersion
    java
    `maven-publish`
}



allprojects {
    group = "com.wanna"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        google()
        mavenLocal()
    }

    apply {
        plugin("java")
        plugin("maven-publish")  // Gradle7.0+, maven插件被移除, 现在使用maven-publish插件
        plugin("kotlin")
        plugin(com.wanna.plugin.BuildPlugin::class.java)
    }

    // 配置maven发布插件的配置, 必须放到apply之后
    publishing {
        publications {
            create<MavenPublication>("maven") {
                afterEvaluate {
                    // 配置发布到Maven仓库时, 需要使用的GAV坐标
                    groupId = this.group.toString()
                    artifactId = this.name
                    version = this.version.toString()

                    // 将Kotlin的源码包添加到发布到Maven仓库的构建, 实现打Jar包的同时打源码包
                    artifact(tasks.getByName("kotlinSourcesJar"))
                }
                from(components["java"])
            }
        }
    }


    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
        implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
        compileOnly("javax.validation:validation-api:$javaxValidationVersion")  // for  validation
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
}