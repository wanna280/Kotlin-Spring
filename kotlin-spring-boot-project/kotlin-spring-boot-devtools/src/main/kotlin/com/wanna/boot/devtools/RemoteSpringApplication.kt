package com.wanna.boot.devtools

import com.wanna.boot.ApplicationType
import com.wanna.boot.Banner
import com.wanna.boot.ResourceBanner
import com.wanna.boot.SpringApplication
import com.wanna.boot.context.logging.LoggingApplicationListener
import com.wanna.boot.devtools.remote.client.RemoteClientConfiguration
import com.wanna.boot.devtools.restart.RestartInitializer
import com.wanna.boot.devtools.restart.RestartScopeInitializer
import com.wanna.boot.devtools.restart.Restarter
import com.wanna.boot.env.EnvironmentPostProcessorApplicationListener
import com.wanna.framework.context.ApplicationContextInitializer
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.core.io.ClassPathResource

/**
 * Remote的SpringApplication, 支持去将本地的配置文件的变更情况, 直接上传给RemoteServer
 *
 * @see RemoteUrlPropertyExtractor
 * @see RemoteClientConfiguration
 */
object RemoteSpringApplication {

    /**
     * main方法, 去启动RemoteSpringApplication Client
     *
     * @param args 命令行参数列表
     */
    @JvmStatic
    fun main(vararg args: String) {
        run(*args)
    }

    @JvmStatic
    fun run(vararg args: String) {
        // 初始化Restarter
        Restarter.initialize(arrayOf(*args), RestartInitializer.NONE)

        // 基于RemoteClientConfiguration作为配置类去构建SpringApplication
        val springApplication = SpringApplication(RemoteClientConfiguration::class.java)

        // set WebApplicationType=NONE
        springApplication.setApplicationType(ApplicationType.NONE)

        // 设置SpringApplication的Banner
        springApplication.setBanner(getBanner())

        // 替换掉默认的Initializer
        springApplication.setInitializers(getInitializers())

        // 替换掉默认的ApplicationListener
        springApplication.setApplicationListeners(getApplicationListeners())

        // run SpringApplication
        springApplication.run(*args)

        // 无限制地等待
        waitIndefinitely()
    }

    /**
     * 获取RemoteClient的Initializer
     *
     * @return 要应用给当前的RemoteClient的ApplicationContextInitializer列表
     */
    @JvmStatic
    private fun getInitializers(): Collection<ApplicationContextInitializer<*>> {
        return listOf(RestartScopeInitializer())
    }

    /**
     * 获取RemoteClient的ApplicationListener
     *
     * @return 要应用给当前的RemoteClient的ApplicationListeners
     */
    @JvmStatic
    private fun getApplicationListeners(): Collection<ApplicationListener<*>> {
        val listeners = ArrayList<ApplicationListener<*>>()

        // 处理配置文件的导入的Listener
        listeners.add(EnvironmentPostProcessorApplicationListener())

        // 从命令参数当中去进行remoteUrl的提取的Listener
        listeners.add(RemoteUrlPropertyExtractor())

        // 日志系统(LoggingSystem)的自动配置的Listener
        listeners.add(LoggingApplicationListener())
        return listeners
    }


    /**
     * 获取RemoteClient的Banner(和[RemoteSpringApplication]在相同的目录下的资源)
     *
     * @return RemoteClient的Banner
     */
    private fun getBanner(): Banner {
        val bannerResource = ClassPathResource("remote-banner.txt", RemoteSpringApplication::class.java)
        return ResourceBanner(bannerResource)
    }

    /**
     * 使用"while(true)"的方式去进行无限制的等待
     */
    @JvmStatic
    private fun waitIndefinitely() {
        while (true) {
            try {
                Thread.sleep(1000L)
            } catch (ex: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }
}

