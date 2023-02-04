package com.wanna.debugger.bistoury.instrument.client.classpath

import java.lang.instrument.Instrumentation

/**
 * 用于从当前VM当中去获取AppLibClass类的默认Supplier,
 * 支持从"bistoury.app.lib.class"这个系统属性去指定的类名, 去找到合适的AppLibClass类.
 * AppLibClass的作用是, 方便根据该类去找到该类所在的Jar包的系统文件路径
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/4
 */
open class DefaultAppLibClassSupplier(private val instrumentation: Instrumentation) : AppLibClassSupplier {

    /**
     * 获取到当前应用的LibClass
     *
     * @return appLibClass
     */
    override fun get(): Class<*> = findAppLibClass(instrumentation)

    companion object {
        /**
         * 用于获取当前应用的AppLibClass的系统属性Key, 用于指定应用程序Jar包当中的类
         */
        private const val BISTOURY_APP_LIB_CLASS_CONFIG_KEY = "bistoury.app.lib.class"

        /**
         * 根据[Instrumentation], 从所有的类当中去找到合适的AppLibClass
         *
         * @param instrumentation Instrumentation
         * @return 从当前VM当中去寻找到的合适的AppLibClass
         *
         * @throws IllegalStateException 如果无法找到合适的AppLibClass
         */
        @Throws(IllegalStateException::class)
        @JvmStatic
        private fun findAppLibClass(instrumentation: Instrumentation): Class<*> {
            val appLibClassName = System.getProperty(BISTOURY_APP_LIB_CLASS_CONFIG_KEY)
            if (appLibClassName.isNullOrBlank()) {
                throw IllegalStateException("cannot find app lib class $appLibClassName")
            }

            for (loadedClass in instrumentation.allLoadedClasses) {
                if (loadedClass.name == appLibClassName) {
                    return loadedClass
                }
            }
            throw IllegalStateException("cannot find app lib class $appLibClassName")
        }
    }
}