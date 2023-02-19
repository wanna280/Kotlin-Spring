dependencies {
    compileOnly(project(":kotlin-debugger-project:kotlin-debugger-bistoury:kotlin-debugger-bistoury-spy"))
}

/**
 * 指定JavaAgent需要回调的类
 */
tasks.getByName<Jar>("jar") {
    manifest.attributes["Premain-Class"] = "com.wanna.debugger.bistoury.instrument.agent.AgentBootstrap"
    manifest.attributes["Agent-Class"] = "com.wanna.debugger.bistoury.instrument.agent.AgentBootstrap"
    manifest.attributes["Can-Redefine-Classes"] = "true"
    manifest.attributes["Can-Retransform-Classes"] = "true"
    manifest.attributes["Can-Set-Native-Method-Prefix"] = "false"
}