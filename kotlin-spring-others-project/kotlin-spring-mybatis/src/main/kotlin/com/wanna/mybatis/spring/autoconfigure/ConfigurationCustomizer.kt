package com.wanna.mybatis.spring.autoconfigure

import org.apache.ibatis.session.Configuration

/**
 * MyBatis的Configuration的自定义化器, 支持对Configuration去进行自定义
 *
 * @see Configuration
 */
interface ConfigurationCustomizer {
    fun customize(configuration: Configuration)
}