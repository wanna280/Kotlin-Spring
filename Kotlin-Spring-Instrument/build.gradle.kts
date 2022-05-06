plugins {
    kotlin("jvm")
    java
}

apply {
    plugin("java")
}

group = "com.wanna"
version = "1.0-SNAPSHOT"

/**
 * 在Kotlin当中，使用task.getByName方法去获取到插件当中的某个任务
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