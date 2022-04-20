package com.wanna.logger.api

import com.wanna.logger.impl.StaticLoggerBinder
import java.io.IOException
import java.net.URL
import java.util.*

/**
 * 这是一个LoggerFactory，提供静态方法，供用户去使用，可以通过静态方法，直接获取到实现方的Logger
 */
class LoggerFactory {
    companion object {

        // STATIC_LOGGER_BINDER的路径
        private const val STATIC_LOGGER_BINDER_PATH = "com/wanna/logger/impl/StaticLoggerBinder.class"
        private const val STATIC_LOGGER_BINDER_CLASSNAME = "com.wanna.logger.impl.StaticLoggerBinder"

        private const val UNINITIALIZED = 0
        private const val ONGOING_INITIALIZATION = 1
        private const val SUCESSFUL_INITIALIZATION = 2
        private const val FAILED_INITIALIZATION = 3

        @Volatile
        @JvmField
        var initialization_state: Int = UNINITIALIZED

        /**
         * 从ILoggerFactory当中去获取Logger，LoggerFactory是交给实现方去进行实现的，API方不负责提供实现
         * 在这里只是为了调用实现方所提供的实现，去完成出初始化LoggerFactory工作，从而获取到Logger
         */
        @JvmStatic
        fun getLogger(clazz: Class<*>): Logger {
            return getILoggerFactory().getLogger(clazz)
        }

        /**
         * 从ILoggerFactory当中去获取Logger，LoggerFactory是交给实现方去进行实现的，API方不负责提供实现
         * 在这里只是为了调用实现方所提供的实现，去完成出初始化LoggerFactory工作，从而获取到Logger
         */
        @JvmStatic
        fun getLogger(name: String): Logger {
            return getILoggerFactory().getLogger(name)
        }

        /**
         * 获取API规范当中的ILoggerFactory，将ILoggerFactory直接暴露给开发者去进行使用
         */
        @JvmStatic
        fun getILoggerFactory(): ILoggerFactory {
            // 如果还没完成初始化，那么需要去执行初始化操作
            if (initialization_state == UNINITIALIZED) {
                synchronized(LoggerFactory::class.java) {
                    if (initialization_state == UNINITIALIZED) {
                        initialization_state = ONGOING_INITIALIZATION
                        performInitialization()
                    }
                }
            }

            // 返回LoggerFactory
            return when (initialization_state) {
                SUCESSFUL_INITIALIZATION -> StaticLoggerBinder.getSingleton().getLoggerFactory()
                else -> throw IllegalStateException("初始化Logger失败，在类路径下找不到合适的StaticLoggerBinder实现类")
            }
        }

        /**
         * 执行StaticLoggerBinder的初始化，确保它在类路径下有提供相应的实现
         */
        private fun performInitialization() {
            try {
                // 加载类路径下的StaticLoggerBinder的类Class文件，并将其路径进行保存
                val staticLoggerPaths = LinkedHashSet<URL>()
                val paths: Enumeration<URL>?
                val loggerClassLoader = LoggerFactory::class.java.classLoader
                if (loggerClassLoader != null) {
                    paths = loggerClassLoader.getResources(STATIC_LOGGER_BINDER_PATH)
                } else {
                    paths = ClassLoader.getSystemResources(STATIC_LOGGER_BINDER_PATH)
                }
                while (paths.hasMoreElements()) {
                    staticLoggerPaths += paths.nextElement()
                }

                // 如果找到了一个以上的StaticLoggerBinder
                if (staticLoggerPaths.size > 1) {
                    System.err.println("在类路径下不止找到这样的一个StaticLoggerBinder")
                    staticLoggerPaths.forEach {
                        System.err.println("在类路径当中找到了StaticLoggerBinder-->[$it]")
                    }
                }
            } catch (ignored: IOException) {

            }
            try {
                val staticLoggerBinderClass = Class.forName(STATIC_LOGGER_BINDER_CLASSNAME)
                val getSingletonMethod = staticLoggerBinderClass.getMethod("getSingleton")
                getSingletonMethod.invoke(staticLoggerBinderClass)
                // getSingleton，确保有该类的存在
                StaticLoggerBinder.getSingleton()
                // 成功初始化
                initialization_state = SUCESSFUL_INITIALIZATION
            } catch (ignored: NoSuchMethodError) {

            } catch (ignored: ClassNotFoundException) {

            }

        }
    }
}