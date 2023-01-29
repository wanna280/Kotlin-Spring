package com.wanna.nacos.api.config

import com.wanna.framework.util.ClassUtils
import com.wanna.nacos.api.PropertyKeyConst
import com.wanna.nacos.api.exception.NacosException
import java.util.*
import kotlin.jvm.Throws

/**
 * Config Factory, 使用反射的方式提供[ConfigService]的创建
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/13
 */
object ConfigFactory {

    @JvmStatic
    @Throws(NacosException::class)
    fun createConfigService(properties: Properties): ConfigService {
        try {
            val clazz = ClassUtils.forName<ConfigService>("com.wanna.nacos.client.config.NacosConfigService")
            val constructor = clazz.getDeclaredConstructor(Properties::class.java)
            return constructor.newInstance(properties)
        } catch (ex: Throwable) {
            throw NacosException("创建ConfigService失败", ex)
        }
    }

    @JvmStatic
    @Throws(NacosException::class)
    fun createConfigService(serverAddr: String): ConfigService {
        val properties = Properties()
        properties[PropertyKeyConst.SERVER_ADDR] = serverAddr
        return createConfigService(properties)
    }
}