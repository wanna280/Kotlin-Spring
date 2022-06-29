

/**
 * 在Kotlin的Gradle DSL当中，使用task.getByName方法去获取到插件当中的某个任务；
 * 这里需要往META-INF/MANIFEST.MF文件当中去添加相关的JavaAgent相关配置信息，才能作为JavaAgent去进行启动；
 * manifest主要用于保存应用程序当中新相关的信息，比如Implementation-Version、Main-Class、Manifest-Version、Created-By、Build-Jdk；
 * 自己也可以定义更多相关的自定义属性到该文件当中
 * Main-Class的定义才能让应用程序可以通过jar -jar的方式去进行启动起来
 */
tasks.getByName<Jar>("jar") {
    manifest.attributes["Premain-Class"] =
        "com.wanna.framework.instrument.InstrumentationSavingAgent"
    manifest.attributes["Agent-Class"] =
        "com.wanna.framework.instrument.InstrumentationSavingAgent"
    manifest.attributes["Can-Redefine-Classes"] = "true"
    manifest.attributes["Can-Retransform-Classes"] = "true"
    manifest.attributes["Can-Set-Native-Method-Prefix"] = "false"
}