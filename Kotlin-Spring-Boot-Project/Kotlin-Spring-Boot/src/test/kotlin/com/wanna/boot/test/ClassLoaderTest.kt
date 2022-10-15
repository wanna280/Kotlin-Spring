package com.wanna.boot.test

class ClassLoaderTest {
}

/**
 * 自定义ClassLoader，去进行loadClass
 */
class MyClassLoader : ClassLoader() {
    override fun loadClass(name: String?): Class<*> {
        val contextClassLoader = Thread.currentThread().contextClassLoader
        val targetClassName = "com.wanna.boot.test.ClassLoaderTest"
        val stream = contextClassLoader.getResourceAsStream(targetClassName.replace(".", "/") + ".class")
        if (name == targetClassName) {
            val bytes = stream!!.readAllBytes()
            return defineClass(targetClassName, bytes, 0, bytes.size)
        }
        return super.loadClass(name)
    }
}

fun main() {
    // 使用不同的ClassLoader，可以去加载用一个类
    val loader = MyClassLoader()
    val clazz = loader.loadClass("com.wanna.boot.test.ClassLoaderTest")
    val clazz2 = ClassLoaderTest::class.java
    println(clazz.classLoader)  // MyClassLoader
    println(clazz2.classLoader)  // AppClassLoader
    println(clazz === clazz2)  // false
}