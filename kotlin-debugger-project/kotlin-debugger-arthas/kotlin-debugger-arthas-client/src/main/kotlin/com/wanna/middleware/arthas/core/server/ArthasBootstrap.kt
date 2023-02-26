package com.wanna.middleware.arthas.core.server

import com.wanna.middleware.arthas.core.config.Configure
import com.wanna.middleware.arthas.core.shell.ShellServer
import com.wanna.middleware.arthas.core.shell.ShellServerOptions
import com.wanna.middleware.arthas.core.shell.impl.ShellServerImpl
import java.lang.instrument.Instrumentation
import javax.annotation.Nullable

/**
 * Arthas的引导启动类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 */
class ArthasBootstrap private constructor(private val pid: Int, private val instrumentation: Instrumentation) {

    fun bind(configure: Configure) {

        val options = ShellServerOptions()
            .setInstrumentation(instrumentation)
            .setPid(pid)

        val shellServer: ShellServer = ShellServerImpl(options, this)

    }


    companion object {
        /**
         * ArthasBootStrap的单例对象
         */
        @Nullable
        @Volatile
        private var arthasBootstrap: ArthasBootstrap? = null

        /**
         * 获取到[ArthasBootstrap]单例对象
         *
         * @param pid pid
         * @param instrumentation Instrumentation, 用于JVM的增强
         */
        @JvmStatic
        fun getInstance(pid: Int, instrumentation: Instrumentation): ArthasBootstrap {
            if (arthasBootstrap == null) {
                synchronized(ArthasBootstrap::class.java) {
                    if (arthasBootstrap == null) {
                        arthasBootstrap = ArthasBootstrap(pid, instrumentation)
                    }
                }
            }
            return arthasBootstrap!!
        }

        /**
         * 获取到[ArthasBootstrap]的单例对象
         *
         * @throws IllegalStateException 如果单例对象还没完成初始化
         */
        @Throws(IllegalStateException::class)
        @JvmStatic
        fun getInstance(): ArthasBootstrap =
            this.arthasBootstrap ?: throw IllegalStateException("ArthasBootstrap must be initialized before!")
    }
}