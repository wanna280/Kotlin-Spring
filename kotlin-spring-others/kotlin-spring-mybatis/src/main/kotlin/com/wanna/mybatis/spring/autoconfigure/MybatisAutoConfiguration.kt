package com.wanna.mybatis.spring.autoconfigure

import com.wanna.boot.autoconfigure.AutoConfigurationPackages
import com.wanna.boot.autoconfigure.condition.ConditionalOnClass
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.boot.autoconfigure.condition.ConditionalOnSingleCandidate
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.definition.GenericBeanDefinition
import com.wanna.framework.context.annotation.*
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.util.StringUtils
import com.wanna.mybatis.spring.SqlSessionFactoryBean
import com.wanna.mybatis.spring.SqlSessionTemplate
import com.wanna.mybatis.spring.mapper.MapperFactoryBean
import com.wanna.mybatis.spring.mapper.MapperScannerConfigurer
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.plugin.Interceptor
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.type.TypeHandler
import org.slf4j.LoggerFactory
import javax.sql.DataSource


/**
 * MyBatis的自动装配, 自动给容器当中去导入SqlSessionTemplate和SqlSessionFactory;
 * 在MapperFactoryBean去进行属性的注入时, 就可以去自动注入SqlSessionFactory和SqlSessionTemplate
 *
 * @see SqlSessionFactory
 * @see SqlSessionTemplate
 */
@ConditionalOnClass([SqlSessionFactory::class, SqlSessionFactoryBean::class])
@EnableConfigurationProperties([MybatisProperties::class])
@ConditionalOnSingleCandidate(DataSource::class)
@Configuration(proxyBeanMethods = false)
open class MybatisAutoConfiguration {
    companion object {

        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(MybatisAutoConfiguration::class.java)
    }

    @Autowired
    private lateinit var properties: MybatisProperties

    @Autowired(required = false)
    private lateinit var interceptors: Array<Interceptor>

    @Autowired(required = false)
    private lateinit var typeHandlers: Array<TypeHandler<*>>

    @Autowired(required = false)
    private lateinit var customizers: Array<ConfigurationCustomizer>

    @Bean
    @ConditionalOnMissingBean
    open fun sqlSessionTemplate(sqlSessionFactory: SqlSessionFactory): SqlSessionTemplate {
        if (properties.executorType != null) {
            return SqlSessionTemplate(sqlSessionFactory, properties.executorType!!)
        }
        return SqlSessionTemplate(sqlSessionFactory)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun sqlSessionFactory(dataSource: DataSource): SqlSessionFactory {
        val sqlSessionFactoryBean = SqlSessionFactoryBean()
        sqlSessionFactoryBean.dataSource = dataSource
        sqlSessionFactoryBean.configLocation = properties.configLocation
        sqlSessionFactoryBean.mapperLocations = properties.mapperLocations
        sqlSessionFactoryBean.typeHandlersPackage = properties.typeHandlersPackage
        sqlSessionFactoryBean.plugins = interceptors
        sqlSessionFactoryBean.typeHandlers = typeHandlers
        return sqlSessionFactoryBean.getObject()
    }

    /**
     * 只要在没有MapperFactoryBean和MapperScannerConfigurer时, 才会导入这个配置类, 并导入AutoConfiguredMapperScannerRegistrar;
     * 主要作用是去提供@Mapper注解的匹配, 将@Mapper的接口扫描到BeanFactory当中; 已经如果有@MapperScan了, 那么@Mapper注解的匹配工作不会生效
     *
     * @see AutoConfiguredMapperScannerRegistrar
     */
    @Import([AutoConfiguredMapperScannerRegistrar::class])
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean([MapperFactoryBean::class, MapperScannerConfigurer::class])
    open class MapperScannerRegistrarNotFoundConfiguration : InitializingBean {
        override fun afterPropertiesSet() {
            if (logger.isDebugEnabled) {
                logger.debug("没有通过@MapperScan/MapperFactoryBean/MapperScannerConfigurer去给MyBatis注册的配置信息")
            }
        }
    }


    /**
     * 自动配置包的MapperScan的注册器, 支持去扫描自动配置包下的所有@Mapper注解的接口;
     * 在没有使用@MapperScan时生效, 在使用了@MapperScan的情况下, @Mapper不生效(这个类不会被导入到Spring容器当中)
     *
     * @see Mapper
     */
    open class AutoConfiguredMapperScannerRegistrar : ImportBeanDefinitionRegistrar, BeanFactoryAware {
        private lateinit var beanFactory: BeanFactory

        override fun registerBeanDefinitions(annotationMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
            if (!AutoConfigurationPackages.has(this.beanFactory)) {
                logger.debug("无法获取到自动配置包, 无法去扫描@Mapper的组件...忽略掉")
                return
            }
            logger.debug("开始去寻找标注了@Mapper注解的接口去进行注册...")


            // 获取SpringBoot的自动配置包的列表
            val packages = AutoConfigurationPackages.get(this.beanFactory)
            if (logger.isDebugEnabled) {
                packages.forEach { logger.debug("正在处理自动配置包[$it]当中的Mapper") }
            }
            val beanDefinition = GenericBeanDefinition(MapperScannerConfigurer::class.java)
            val packagesString = StringUtils.collectionToCommaDelimitedString(packages)
            val propertyValues = beanDefinition.getPropertyValues()
            propertyValues.addPropertyValue("processPropertyPlaceHolders", true)  // 要处理占位符
            propertyValues.addPropertyValue("annotationClass", Mapper::class.java)  // 设置要匹配的注解为@Mapper
            propertyValues.addPropertyValue("basePackages", packagesString)  // 扫描的包

            registry.registerBeanDefinition(MapperScannerConfigurer::class.java.name, beanDefinition)
        }

        override fun setBeanFactory(beanFactory: BeanFactory) {
            this.beanFactory = beanFactory
        }
    }
}