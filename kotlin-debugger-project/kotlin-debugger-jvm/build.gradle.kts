tasks.withType<JavaCompile>().all {
    this.options.compilerArgs.add("--add-exports=jdk.jdi/com.sun.tools.jdi=ALL-UNNAMED")
}