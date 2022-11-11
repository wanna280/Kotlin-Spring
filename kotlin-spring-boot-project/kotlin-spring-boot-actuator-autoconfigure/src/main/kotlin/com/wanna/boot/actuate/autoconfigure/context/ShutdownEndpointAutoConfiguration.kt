package com.wanna.boot.actuate.autoconfigure.context

import com.wanna.boot.actuate.context.ShutdownEndpoint
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * Shutdown的Endpoint的自动配置类，当访问这个接口时，会自动关闭SpringBoot应用
 *
 * @see ShutdownEndpoint
 */
@Configuration(proxyBeanMethods = false)
open class ShutdownEndpointAutoConfiguration {

    /**
     * 给SpringBeanFactory当中去注册一个Shutdown的Endpoint
     *
     * @param applicationContext 要去进行关闭的ApplicationContext
     */
    @Bean
    open fun shutdownEndpoint(applicationContext: ConfigurableApplicationContext): ShutdownEndpoint =
        ShutdownEndpoint(applicationContext)
}