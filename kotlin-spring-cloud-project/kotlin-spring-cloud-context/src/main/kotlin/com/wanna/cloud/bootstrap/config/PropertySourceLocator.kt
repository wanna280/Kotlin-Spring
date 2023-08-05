package com.wanna.cloud.bootstrap.config

import com.wanna.framework.core.environment.CompositePropertySource
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.PropertySource

/**
 * 它是为本地(或者远程)的配置文件提供加载的策略, 实现方不能失败, 除非它想在启动过程当中去阻止SpringApplication; 
 * 实现远程的配置文件的拉取, 主要就是通过这个组件, 去完成的加载, 比如Nacos就会实现自定义的PropertySourceLocator去完成服务配置中心的加载; 
 */
@FunctionalInterface
interface PropertySourceLocator {

    /**
     * 加载一个PropertySource
     *
     * @param environment Environment
     * @return 加载到的PropertySource, 有可能为null
     */
    fun locate(environment: Environment): PropertySource<*>?

    /**
     * locate一个集合, 如果返回的PropertySource是一个CompositePropertySource的话, 那么返回一个PropertySource列表
     *
     * @param environment 环境对象
     */
    fun locateCollection(environment: Environment): Collection<PropertySource<*>> {
        return locateCollection(this, environment)
    }

    companion object {
        /**
         * locate一个集合, 如果返回的PropertySource是一个CompositePropertySource的话, 那么返回一个PropertySource列表
         *
         * @param locator Locator
         * @param environment 环境对象
         */
        fun locateCollection(locator: PropertySourceLocator, environment: Environment): Collection<PropertySource<*>> {
            val propertySource = locator.locate(environment) ?: return emptyList()
            return if (propertySource is CompositePropertySource) {
                propertySource.getPropertySources().toList()
            } else {
                listOf(propertySource)
            }
        }
    }
}