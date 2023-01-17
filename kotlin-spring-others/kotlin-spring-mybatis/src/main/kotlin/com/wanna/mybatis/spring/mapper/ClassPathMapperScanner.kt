package com.wanna.mybatis.spring.mapper

import com.wanna.framework.beans.factory.FactoryBean
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.config.ConfigurableBeanFactory
import com.wanna.framework.beans.factory.config.RuntimeBeanReference
import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import com.wanna.framework.beans.factory.support.definition.AbstractBeanDefinition
import com.wanna.framework.context.annotation.ClassPathBeanDefinitionScanner
import com.wanna.framework.core.type.filter.AnnotationTypeFilter
import com.wanna.framework.core.type.filter.AssignableTypeFilter
import com.wanna.framework.core.type.filter.TypeFilter
import com.wanna.framework.util.BeanUtils
import com.wanna.mybatis.spring.SqlSessionTemplate
import org.apache.ibatis.session.SqlSessionFactory
import org.slf4j.LoggerFactory
import java.util.Optional

/**
 * 类路径下的Mapper的扫描器, 负责扫描类路径下的合适的接口作为Mapper, 并将Mapper接口的BeanDefinition替换成为MapperFactoryBean
 *
 * Note: useDefaultFilters=true, 添加@Component注解的匹配, 这里设置useDefaultFilters=false, 自定义TypeFilter
 *
 * @param registry registry
 * @see ClassPathBeanDefinitionScanner
 * @see com.wanna.mybatis.spring.annotation.MapperScan
 */
open class ClassPathMapperScanner(registry: BeanDefinitionRegistry) : ClassPathBeanDefinitionScanner(registry, false) {

    companion object {
        private val logger = LoggerFactory.getLogger(ClassPathMapperScanner::class.java)
    }

    // 是否要添加Mapper到MyBatis的Configuration当中
    var addToConfig = true

    // 是否要懒加载? 默认为false
    var lazyInitialization = false

    // SqlSessionFactory
    var sqlSessionFactory: SqlSessionFactory? = null

    // SqlSessionTemplate
    var sqlSessionTemplate: SqlSessionTemplate? = null

    // sqlSessionTemplateBeanName
    var sqlSessionTemplateBeanName: String? = null

    // sqlSessionFactoryBeanName
    var sqlSessionFactoryBeanName: String? = null

    // 要匹配的注解, 比如@Mapper
    var annotationClass: Class<out Annotation?>? = null

    // 要匹配的父类?
    var markerInterface: Class<*>? = null

    // 要使用的MapperFactoryBeanClass? 可以支持去自定义
    var mapperFactoryBeanClass: Class<out MapperFactoryBean?> = MapperFactoryBean::class.java

    // 默认的scope, Bean的作用域
    var defaultScope: String? = null

    /**
     * 注册处理注解/指定markerInterface的所有的Mapper接口的匹配规则的TypeFilter
     *
     * * 1.如果需要匹配注解的话, 那么添加注解的匹配器
     * * 2.如果需要匹配接口的话, 那么添加类型的匹配器
     * * 3.如果接口和类型都不需要去进行匹配的话, 那么添加一个放行所有接口的匹配器
     */
    open fun registerFilters() {
        var acceptAllInterfaces = true
        // 如果需要匹配注解的话, 那么添加一个TypeFilter
        Optional.ofNullable(this.annotationClass).ifPresent {
            addIncludeFilter(AnnotationTypeFilter(it))
            acceptAllInterfaces = false
        }

        // 如果需要匹配父接口的话, 那么添加一个TypeFilter
        Optional.ofNullable(this.markerInterface).ifPresent {
            addIncludeFilter(AssignableTypeFilter(it))
            acceptAllInterfaces = false
        }

        // 如果接收所有的接口的话(不匹配父接口, 也不匹配注解, 就是接收所有接口)...在这里去注册一个匹配所有的接口的Filter
        if (acceptAllInterfaces) {
            addIncludeFilter { _, _ -> true }
        }
    }

    /**
     * 重写父类的扫描BeanDefinition的逻辑的方式, 对BeanDefinition去进行自定义处理, 因为扫描出来的BeanDefinition的beanClass还是接口,
     * 而我们肯定会需要去进行实例化, 因此, 我们需要替换目标beanClass
     *
     * @param packages 要扫描的包
     * @return 扫描到的BeanDefinitionHolder列表
     */
    override fun doScan(vararg packages: String): Set<BeanDefinitionHolder> {
        val beanDefinitionHolders = super.doScan(*packages)
        processBeanDefinitions(beanDefinitionHolders)
        return beanDefinitionHolders
    }

    /**
     * 处理扫描到的BeanDefinition, 需要将beanClass替换成为MapperFactoryBean,
     * 并设置SqlSessionFactory和SqlSessionTemplate等属性
     *
     * @param beanDefinitions 扫描出来的beanDefinitions列表
     */
    private fun processBeanDefinitions(beanDefinitions: Set<BeanDefinitionHolder>) {
        beanDefinitions.forEach { holder ->
            val beanDefinition = holder.beanDefinition as AbstractBeanDefinition
            val propertyValues = beanDefinition.getPropertyValues()
            val beanClass = beanDefinition.getBeanClass()
            if (logger.isTraceEnabled) {
                logger.trace("创建MapperFactoryBean中, 使用[beanName=${holder.beanName}], mapperInterface=[$beanClass]")
            }
            propertyValues.addPropertyValue("mapperInterface", beanClass)

            // 设置实例的Supplier, 因为需要构造器当中提供接口, 不然实例化过程中会有bug, 提前获取beanType, 但是beanType还没设置进去
            // 但是目前针对构造器参数的给出, 并未提供实现, 因此暂时使用Supplier的方式去进行提供实现
            beanDefinition.setInstanceSupplier {
                BeanUtils.instantiateClass(
                    mapperFactoryBeanClass.getConstructor(Class::class.java),
                    mapperFactoryBeanClass
                )
            }

            // for predict beanType
            beanDefinition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE, beanClass)
            propertyValues.addPropertyValue("addToConfig", this.addToConfig)

            // 是否已经找到了合适的SqlSessionFactory?
            var explicitFactoryUsed = false

            // 添加SqlSessionFactory, 目的是为了回调MapperFactoryBean.setSqlSessionFactory方法
            if (sqlSessionFactoryBeanName != null) {
                propertyValues.addPropertyValue("sqlSessionFactory", RuntimeBeanReference(sqlSessionFactoryBeanName!!))
                explicitFactoryUsed = true
            } else if (sqlSessionFactory != null) {
                propertyValues.addPropertyValue("sqlSessionFactory", sqlSessionFactory)
                explicitFactoryUsed = true
            }

            // 添加setSqlSessionTemplate, 目的是为了回调MapperFactoryBean.setSqlSessionTemplate方法
            if (sqlSessionTemplateBeanName != null) {
                if (explicitFactoryUsed) {
                    logger.warn("已经设置了SqlSessionFactory, 但是又想要去设置SqlSessionTemplate, 之前的SqlSessionFactory将会失效, 将会采用指定的SqlSessionTemplate")
                }
                propertyValues.addPropertyValue(
                    "sqlSessionTemplate", RuntimeBeanReference(sqlSessionTemplateBeanName!!)
                )
                explicitFactoryUsed = true
            } else if (sqlSessionTemplate != null) {
                if (explicitFactoryUsed) {
                    logger.warn("已经设置了SqlSessionFactory, 但是又想要去设置SqlSessionTemplate, 之前的SqlSessionFactory将会失效, 将会采用指定的SqlSessionTemplate")
                }
                propertyValues.addPropertyValue("sqlSessionTemplate", sqlSessionTemplate)
                explicitFactoryUsed = true
            }

            // 替换掉beanClass并设置LazyInit(是否懒加载? )
            beanDefinition.setBeanClass(mapperFactoryBeanClass)
            beanDefinition.setLazyInit(this.lazyInitialization)

            // 如果没有找到合适的SqlSessionFactory(SqlSessionTemplate也算), 那么设置为byType自动注入, 交给Spring去自动去注入SqlSessionFactory和SqlSessionTemplate
            if (!explicitFactoryUsed) {
                beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE)
            }

            // set Scope
            if (beanDefinition.getScope() == ConfigurableBeanFactory.SCOPE_SINGLETON && this.defaultScope != null) {
                beanDefinition.setScope(defaultScope!!)
            }
        }
    }
}