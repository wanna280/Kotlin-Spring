package com.wanna.nacos.api

import com.wanna.nacos.api.config.ConfigFactory
import com.wanna.nacos.api.config.ConfigService
import com.wanna.nacos.api.exception.NacosException
import com.wanna.nacos.api.naming.NamingFactory
import com.wanna.nacos.api.naming.NamingService
import java.util.*
import kotlin.jvm.Throws

/**
 * 提供对于Nacos的[NamingService]和[ConfigService]的创建
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/13
 */
object NacosFactory {

    /**
     * 根据给定的[Properties]配置信息去创建出来[ConfigService]
     *
     * @param properties Properties配置信息
     * @return ConfigService
     */
    @JvmStatic
    @Throws(NacosException::class)
    fun createConfigService(properties: Properties): ConfigService {
        return ConfigFactory.createConfigService(properties)
    }

    /**
     * 根据给定的serverAddr配置去创建出来[ConfigService]
     *
     * @param serverAddr configServer地址
     * @return 创建出来的ConfigService
     */
    @JvmStatic
    @Throws(NacosException::class)
    fun createConfigService(serverAddr: String): ConfigService {
        return ConfigFactory.createConfigService(serverAddr)
    }

    /**
     * 根据给定的[Properties]配置信息去创建出来[NamingService]
     *
     * @param properties Properties配置信息
     * @return NamingService
     */
    @JvmStatic
    @Throws(NacosException::class)
    fun createNamingService(properties: Properties): NamingService {
        return NamingFactory.createNamingService(properties)
    }

    /**
     * 根据给定的serverAddr配置去创建出来[NamingService]
     *
     * @param serverAddr namingServer地址
     * @return 创建出来的NamingService
     */
    @JvmStatic
    @Throws(NacosException::class)
    fun createNamingService(serverAddr: String): NamingService {
        return NamingFactory.createNamingService(serverAddr)
    }
}