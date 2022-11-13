package com.wanna.nacos.api.naming

import com.wanna.framework.util.ClassUtils
import com.wanna.nacos.api.exception.NacosException
import java.util.*
import kotlin.jvm.Throws

/**
 * Naming Factory, 使用反射的方式去提供[NamingService]的创建
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/13
 */
object NamingFactory {

    @JvmStatic
    @Throws(NacosException::class)
    fun createNamingService(properties: Properties): NamingService {

        try {
            val clazz = ClassUtils.forName<NamingService>("com.wanna.nacos.client.naming.NacosNamingService")
            val constructor = clazz.getDeclaredConstructor(Properties::class.java)
            return constructor.newInstance(properties)
        } catch (ex: Throwable) {
            throw NacosException("创建NamingService失败", ex)
        }
    }

    @JvmStatic
    @Throws(NacosException::class)
    fun createNamingService(serverAddr: String): NamingService {
        try {
            val clazz = ClassUtils.forName<NamingService>("com.wanna.nacos.client.naming.NacosNamingService")
            val constructor = clazz.getDeclaredConstructor(String::class.java)
            return constructor.newInstance(serverAddr)
        } catch (ex: Throwable) {
            throw NacosException("创建NamingService失败", ex)
        }
    }
}