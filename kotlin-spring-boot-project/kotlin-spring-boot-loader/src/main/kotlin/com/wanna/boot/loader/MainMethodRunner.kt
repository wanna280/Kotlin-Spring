package com.wanna.boot.loader

/**
 * Main方法的Runner
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/2
 *
 * @param mainClassName 主类名
 * @param args 方法参数(命令行参数)
 */
class MainMethodRunner(private val mainClassName: String, private val args: Array<String>) {

    /**
     * 执行main方法, 去进行启动应用(使用的是ContextClassLoader去进行的类加载)
     *
     * @throws Exception 如果启动应用应用过程当中发生了异常
     */
    @Throws(Exception::class)
    fun run() {
        val mainClass = Class.forName(mainClassName, false, Thread.currentThread().contextClassLoader)
        val mainMethod = mainClass.getDeclaredMethod("main", Array<String>::class.java)
        mainMethod.isAccessible = true
        mainMethod.invoke(null, arrayOf(*args))
    }
}