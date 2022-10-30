package com.wanna.framework.web.context

import com.wanna.framework.context.ConfigurableApplicationContext

/**
 * 这是一个可以支持配置的ReactiveWebApplicationContext
 *
 * @see MvcWebApplicationContext
 * @see ConfigurableApplicationContext
 */
interface ConfigurableMvcWebApplicationContext : MvcWebApplicationContext, ConfigurableApplicationContext