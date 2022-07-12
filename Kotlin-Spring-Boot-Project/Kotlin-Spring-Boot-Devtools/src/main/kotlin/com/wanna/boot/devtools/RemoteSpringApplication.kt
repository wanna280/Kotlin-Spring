package com.wanna.boot.devtools

import com.wanna.boot.ApplicationContextInitializer
import com.wanna.boot.ApplicationType
import com.wanna.boot.SpringApplication
import com.wanna.boot.devtools.remote.client.RemoteClientConfiguration
import com.wanna.boot.devtools.restart.RestartScopeInitializer
import com.wanna.boot.env.EnvironmentPostProcessorApplicationListener
import com.wanna.framework.context.event.ApplicationListener

/**
 * Remote的SpringApplication，支持去将本地的配置文件的变更情况，直接上传给RemoteServer
 *
 * @see RemoteUrlPropertyExtractor
 * @see RemoteClientConfiguration
 */
class RemoteSpringApplication {

    fun run(vararg args: String) {
        val springApplication = SpringApplication(RemoteClientConfiguration::class.java)

        // set WebApplicationType=NONE
        springApplication.setApplicationType(ApplicationType.NONE)

        // 替换掉默认的Initializer
        springApplication.setInitializers(getInitializers())

        // 替换掉默认的ApplicationListener
        springApplication.setApplicationListeners(getApplicationListeners())

        // run SpringApplication
        springApplication.run(*args)

        // 无限制地等待
        waitIndefinitely()
    }

    private fun getInitializers(): Collection<ApplicationContextInitializer<*>> {
        return listOf(RestartScopeInitializer())
    }

    private fun getApplicationListeners(): Collection<ApplicationListener<*>> {
        val listeners = ArrayList<ApplicationListener<*>>()
        listeners.add(EnvironmentPostProcessorApplicationListener())
        listeners.add(RemoteUrlPropertyExtractor())
        return listeners
    }

    /**
     * 使用"while(true)"的方式去进行无限制的等待
     */
    private fun waitIndefinitely() {
        while (true) {
            try {
                Thread.sleep(1000L)
            } catch (ex: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            RemoteSpringApplication().run(*args)
        }
    }
}

