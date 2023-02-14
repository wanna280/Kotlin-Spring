package com.wanna.boot.loader

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/15
 */
class PropertiesLauncherTest {

    companion object {

        @JvmStatic
        fun main(vararg args: String) {
            println("test application running")
        }
    }
}


fun useSystemProperties() {
    System.setProperty("loader.main", "com.wanna.boot.loader.PropertiesLauncherTest")
    System.setProperty("loader.classLoader", "com.wanna.boot.loader.PropertiesLauncherClassLoader")
    PropertiesLauncher.main()
}

fun useLoaderProperties() {
    PropertiesLauncher.main()
}

fun main() {
    System.setProperty("loader.debug", "true")
    useLoaderProperties()
    println()
}