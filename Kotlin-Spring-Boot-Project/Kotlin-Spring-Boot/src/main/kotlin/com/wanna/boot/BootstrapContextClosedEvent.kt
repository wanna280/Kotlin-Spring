package com.wanna.boot

import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.event.ApplicationEvent

/**
 * BootstrapContext已经关闭的事件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 *
 * @param source source BootstrapContext
 * @param applicationContext ApplicationContext
 */
open class BootstrapContextClosedEvent(
    val source: BootstrapContext,
    val applicationContext: ConfigurableApplicationContext
) : ApplicationEvent(source)