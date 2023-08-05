package com.wanna.boot.admin

import com.wanna.boot.SpringApplication
import com.wanna.boot.context.event.ApplicationReadyEvent
import com.wanna.boot.web.server.WebServerInitializedEvent
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.lang.Nullable

/**
 * 一个通过JMX去用于去监控和管理[SpringApplication]的MBean, 仅仅是为了内部使用
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/17
 */
interface SpringApplicationAdminMXBean {

    /**
     * 当前应用是否已经完全启动成功了?
     *
     * @return 如果当前应用已经完全准备好, return true; 否则return false
     *
     * @see ApplicationReadyEvent
     */
    fun isReady(): Boolean

    /**
     * 当前应用是否是一个嵌入式的Web应用? 如果这个应用还没完全启动, 那么也会return false; 因此对于等待应用的启动, 使用[isReady]更合适
     *
     * @return 如果当前应用运行在嵌入式的Web容器当中, 并且应用已经完全启动, return true; 否则return false
     * @see WebServerInitializedEvent
     */
    fun isEmbeddedWebApplication(): Boolean

    /**
     * 从Spring应用的[Environment]当中去根据属性Key去获取到对应的属性值
     *
     * @param key 属性Key
     * @return 根据属性Key获取到的属性值(获取不到return null)
     */
    @Nullable
    fun getProperty(key: String): String?

    /**
     * 关闭当前Application
     *
     * @see ConfigurableApplicationContext.close
     */
    fun shutdown()

}