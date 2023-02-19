dependencies {
    implementation("org.ow2.asm:asm:$asmVersion")
    implementation("org.ow2.asm:asm-util:$asmVersion")
    implementation("org.ow2.asm:asm-commons:$asmVersion")
    implementation("org.ow2.asm:asm-analysis:$asmVersion")
    implementation("org.ow2.asm:asm-tree:$asmVersion")

    implementation(project(":kotlin-metrics-project:kotlin-metrics"))
    implementation(project(":kotlin-debugger-project:kotlin-debugger-bistoury:kotlin-debugger-bistoury-spy"))
}
