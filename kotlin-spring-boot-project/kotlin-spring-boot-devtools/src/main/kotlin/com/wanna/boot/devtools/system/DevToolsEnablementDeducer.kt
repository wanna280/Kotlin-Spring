package com.wanna.boot.devtools.system

/**
 * DevTools是否开启的探测器, 去检查启用SpringApplication的线程的栈轨迹当中
 * 是否有是来自于"test"框架的方法, 如果有来自"test"框架的栈轨迹的话, 说明我们不应该启用DevTools, 我们
 * 只会在使用"main"方法去进行启动的情况下, 才需要去启用DevTools
 */
object DevToolsEnablementDeducer {

    /**
     * 应该去进行跳过的栈轨迹元素的前缀, 来自于"test"框架, 对于测试框架的情况下, 我们不应该使用DevTools
     */
    @JvmStatic
    private val SKIPPED_STACK_ELEMENTS = setOf(
        "org.junit.runners.",
        "org.junit.platform.",
        "cucumber.runtime."
    )

    /**
     * 从给定的线程的栈轨迹当中去推断是否应该启用DevTools
     *
     * @param thread 给定的用来去探测栈轨迹的线程
     * @return 是否应该启用DevTools? 如果存在有"test"的栈轨迹, return false; 否则return true
     */
    @JvmStatic
    fun shouldEnable(thread: Thread): Boolean {
        thread.stackTrace.forEach {
            // 如果存在有"test"的元素, 说明就不应该启用DevTools
            if (isSkippedStackElement(it)) {
                return false
            }
        }
        return true
    }

    /**
     * 给定一个StackTraceElement, 去检查该元素是否应该去进行跳过, 
     * 如果它是相关的"test"的实现(比如"junit"), 那么就应该去进行跳过
     *
     * @param stackTraceElement StackTraceElement
     * @return 如果该元素是来自"test", 那么return true; 否则return false
     */
    @JvmStatic
    private fun isSkippedStackElement(stackTraceElement: StackTraceElement): Boolean {
        SKIPPED_STACK_ELEMENTS.forEach {
            if (stackTraceElement.className.startsWith(it)) {
                return true
            }
        }
        return false
    }
}