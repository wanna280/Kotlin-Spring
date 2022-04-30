package com.wanna.boot.web.reactive.context

import com.wanna.framework.context.ConfigurableApplicationContext

/**
 * 这是一个可以支持配置的ReactiveWebApplicationContext
 */
interface ConfigurableReactiveWebApplicationContext : ReactiveWebApplicationContext, ConfigurableApplicationContext