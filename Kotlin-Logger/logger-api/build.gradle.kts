plugins {
    kotlin("jvm")
    java
}

group = "com.wanna"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    // 这个依赖只是一个编译时依赖，通过编译手段，让编译时可以解析到对应的依赖
    // 在运行时，不会存在有相关的LoggerImpl的实现依赖，需要用户自己完成StaticLoggerBinder的导入
    compileOnly(project(":Kotlin-Logger:logger-temp"))
}
