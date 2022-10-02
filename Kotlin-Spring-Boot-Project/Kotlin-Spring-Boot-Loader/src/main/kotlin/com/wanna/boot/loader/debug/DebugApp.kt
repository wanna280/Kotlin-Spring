package com.wanna.boot.loader.debug

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/3
 */
class DebugApp {
    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            println(DebugApp::class.java.classLoader)
        }
    }
}