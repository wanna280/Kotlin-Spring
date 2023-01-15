package com.wanna.cloud.nacos.config

import com.wanna.cloud.bootstrap.config.PropertySourceLocator
import com.wanna.framework.core.environment.CompositePropertySource
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.PropertySource

/**
 * Nacos的PropertySourceLocator, 它负责去实现从远程拉取配置文件到本地, 并配置到Environment当中
 *
 * @param nacosConfigManager NacosConfigManager
 */
open class NacosPropertySourceLocator(private val nacosConfigManager: NacosConfigManager) : PropertySourceLocator {

    companion object {
        /**
         * Nacos的PropertySource的名字
         */
        const val NACOS_CONFIG_PROPERTY_SOURCE_NAME = "nacosProperties"
    }

    /**
     * NacosConfigProperties
     */
    private val nacosConfigProperties = nacosConfigManager.nacosConfigProperties


    /**
     * 加载得到[PropertySource]
     *
     * @param environment Environment
     * @return 加载得到的PropertySource
     */
    override fun locate(environment: Environment): PropertySource<*>? {
        // 给NacosConfigProperties去设置Environment
        nacosConfigProperties.environment = environment
        nacosConfigProperties.getNacosConfigProperties()  // init for Properties

        // 创建一个组合的Nacos的PropertySource
        val composite = CompositePropertySource(NACOS_CONFIG_PROPERTY_SOURCE_NAME)

        val prefix = nacosConfigProperties.prefix
        val fileExtension = nacosConfigProperties.fileExtension
        val group = nacosConfigProperties.group
        val timeout = nacosConfigProperties.timeout

        // 加载Nacos配置文件

        // 1.尝试使用prefix和group加载
        loadIfAbsent(composite, prefix, group, timeout)
        // 2.尝试使用prefix.extension和group加载
        loadIfAbsent(composite, "$prefix.$fileExtension", group, timeout)

        return composite
    }

    /**
     * 如果必要的话, 去加载Nacos的Properties, 并将结果添加到CompositePropertySource当中
     *
     * @param composite CompositePropertySource(输出参数)
     * @param dataId dataId
     * @param group group
     * @param timeout timeout
     */
    protected open fun loadIfAbsent(composite: CompositePropertySource, dataId: String, group: String, timeout: Long) {
        // 从NacosConfigManager去拉取配置文件...
        val properties = nacosConfigManager.getConfig(dataId, group, timeout)
        val nacosPropertySource = NacosPropertySource(dataId, group, properties)
        composite.addFirstPropertySource(nacosPropertySource)

        // 将NacosPropertySource添加到仓库当中, 方便别的地方去进行使用
        NacosPropertySourceRepository.registerNacosPropertySource(nacosPropertySource)
    }
}