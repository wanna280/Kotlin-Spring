import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version ("6.1.0")
}


dependencies {
    implementation("cglib:cglib:$cglibVersion")
    implementation(project(":kotlin-spring-framework-project:kotlin-spring-jcl"))

    compileOnly(fileTree("${project.buildDir}/libs") {
        this.include("*.jar")
    })
}


// 获取到ShadowJar任务
val shadowJar = tasks.getByName<ShadowJar>("shadowJar")


// 自定义一个ShadowJar任务, 用于去将Cglib/ASM包下的类直接重定向到"com.wanna.framework.core"下
tasks.register<ShadowJar>("kotlinSpringCglibRepackJar") {
    // 生成的目标文件名
    archiveFileName.set("kotlin-spring-cglib-repack-$kotlinVersion.jar")

    // Note: 只有设置Configuration才能输出文件!!!!!!不设置啥也没有
    this.configurations = shadowJar.configurations

    // 将"net.sf.cglib"去重定向到"com.wanna.framework.cglib"下
    relocate("net.sf.cglib", "com.wanna.framework.cglib")

    // 将"org.objectweb.asm"去重定向到"com.wanna.framework.core.asm"下
    relocate("org.objectweb.asm", "com.wanna.framework.asm")
}


// 将kotlinSpringCglibRepackJar任务去附加到Jar任务上, 在打Jar包的时候, copy所有的"com/wanna"下的文件
tasks.getByName<Jar>("jar") {
    val kotlinSpringCglibRepackJar = tasks.getByName<ShadowJar>("kotlinSpringCglibRepackJar")
    dependsOn(kotlinSpringCglibRepackJar)
    from(zipTree(kotlinSpringCglibRepackJar.archiveFile.get().asFile.absolutePath)) {
        include("com/wanna/**")
        exclude("com/wanna/framework/cglib/proxy/Enhancer*.class")
        exclude("com/wanna/framework/cglib/proxy/MethodProxy*.class")
        exclude("com/wanna/framework/cglib/proxy/MethodInterceptor*.class")
        exclude("com/wanna/framework/cglib/proxy/Callback*.class")

        exclude("com/wanna/framework/cglib/core/ReflectUtils*.class")
        exclude("com/wanna/framework/cglib/core/ClassGenerator*.class")
        exclude("com/wanna/framework/cglib/core/AbstractClassGenerator*.class")
    }
}