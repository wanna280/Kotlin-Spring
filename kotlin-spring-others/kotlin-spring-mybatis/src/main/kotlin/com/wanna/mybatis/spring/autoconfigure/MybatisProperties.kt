package com.wanna.mybatis.spring.autoconfigure

import com.wanna.boot.context.properties.ConfigurationProperties
import org.apache.ibatis.session.ExecutorType

/**
 * MyBatis的一些配置信息
 */
@ConfigurationProperties("mybatis")
open class MybatisProperties {
    var executorType: ExecutorType? = null

    // configLocation
    var configLocation: String? = null

    // mapperLocations
    var mapperLocations: Array<String>? = null

    // TypeHandler所在的包
    var typeHandlersPackage: String? = null
}