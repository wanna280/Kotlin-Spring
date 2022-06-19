package com.wanna.mybatis.spring

import com.wanna.framework.beans.factory.FactoryBean
import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.util.StreamUtils
import com.wanna.mybatis.spring.transaction.SpringManagedTransactionFactory
import org.apache.ibatis.builder.xml.XMLConfigBuilder
import org.apache.ibatis.builder.xml.XMLMapperBuilder
import org.apache.ibatis.cache.Cache
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.plugin.Interceptor
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory
import org.apache.ibatis.transaction.TransactionFactory
import org.apache.ibatis.type.TypeHandler
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.Optional
import java.util.Properties
import java.util.stream.Stream
import javax.sql.DataSource

/**
 * SqlSessionFactoryBean，负责给容器当中去导入SqlSessionFactory
 *
 * @see SqlSessionFactory
 */
open class SqlSessionFactoryBean : FactoryBean<SqlSessionFactory>, InitializingBean {

    // 事务工厂(如果不设置，默认将会采用SpringManagedTransactionFactory)
    var transactionFactory: TransactionFactory? = null

    // Configuration
    var configuration: Configuration? = null

    // MyBatis的拦截器，也就是要应用的插件
    var plugins: Array<Interceptor>? = null

    // 自定义的配置属性
    private var configurationProperties: Properties? = null

    // environment name
    private var environment: String = SqlSessionFactoryBean::class.java.name

    // TypeHandlers
    var typeHandlers: Array<TypeHandler<*>>? = null

    // 默认的EnumTypeHandler
    var defaultEnumTypeHandler: Class<out TypeHandler<*>>? = null

    // Cache
    var cache: Cache? = null

    // TypeHandler所在的包
    var typeHandlersPackage: String? = null

    // SqlSessionFactory
    var sqlSessionFactory: SqlSessionFactory? = null

    // 配置文件路径
    var configLocation: String? = null

    // MapperLocations
    var mapperLocations: Array<String>? = null

    // 要使用的数据源，提供MyBatis事务的连接的获取
    var dataSource: DataSource? = null

    // 获取SqlSessionFactory的类型
    override fun getObjectType(): Class<out SqlSessionFactory> =
        if (sqlSessionFactory == null) SqlSessionFactory::class.java else sqlSessionFactory!!::class.java

    override fun afterPropertiesSet() {
        // 创建MyBatis的Configuration对象
        val targetConfiguration: Configuration
        if (this.configuration != null) {
            targetConfiguration = this.configuration!!
            if (targetConfiguration.variables == null) {
                targetConfiguration.variables = configurationProperties
            } else {
                Optional.ofNullable(configurationProperties).ifPresent(targetConfiguration.variables::putAll)
            }
        } else if (configLocation != null) {
            targetConfiguration =
                XMLConfigBuilder(getInputStream(configLocation!!), null, configurationProperties).configuration
        } else {
            targetConfiguration = Configuration()
            Optional.ofNullable(this.configurationProperties).ifPresent(targetConfiguration::setVariables)
        }
        // 添加要apply的插件
        Optional.ofNullable(this.plugins)
            .ifPresent { it.forEach { interceptor -> targetConfiguration.addInterceptor(interceptor) } }

        // 添加TypeHandler
        Optional.ofNullable(this.typeHandlers).ifPresent {
            it.forEach { typeHandler ->
                targetConfiguration.typeHandlerRegistry.typeHandlers.add(typeHandler)
            }
        }
        Optional.ofNullable(this.cache).ifPresent(targetConfiguration::addCache)
        Optional.ofNullable(this.typeHandlersPackage).ifPresent(targetConfiguration.typeHandlerRegistry::register)
        Optional.ofNullable(this.defaultEnumTypeHandler).ifPresent(targetConfiguration::setDefaultEnumTypeHandler)

        // set Environment
        targetConfiguration.environment =
            Environment(environment, this.transactionFactory ?: SpringManagedTransactionFactory(), this.dataSource)

        // 处理给定的MapperLocations，解析Xml的Mapper配置文件
        Optional.ofNullable(this.mapperLocations).ifPresent {
            it.forEach { location ->
                XMLMapperBuilder(
                    getInputStream(location), targetConfiguration, location, targetConfiguration.sqlFragments
                ).parse()
            }
        }
        this.sqlSessionFactory = DefaultSqlSessionFactory(targetConfiguration)
    }

    override fun getObject(): SqlSessionFactory {
        if (sqlSessionFactory == null) {
            afterPropertiesSet()
        }
        return sqlSessionFactory ?: throw IllegalStateException("SqlSessionFactory不能为空")
    }

    /**
     * 给定资源路径，获取该资源的输入流
     *
     * @param location 资源路径
     * @throws FileNotFoundException 如果给定的文件没有找到
     */
    private fun getInputStream(location: String): InputStream {
        try {
            if (location.startsWith("classpath:")) {
                return SqlSessionFactoryBean::class.java.classLoader.getResourceAsStream(location.substring("classpath:".length))!!
            }
            return FileInputStream(location)
        } catch (ex: Exception) {
            when (ex) {
                is IOException -> throw FileNotFoundException("给定的文件路径[$location]没有找到")
                is NullPointerException -> throw FileNotFoundException("给定的文件路径[$location]没有找到")
                else -> throw ex
            }
        }
    }

    override fun isSingleton(): Boolean = true

    override fun isPrototype(): Boolean = false
}