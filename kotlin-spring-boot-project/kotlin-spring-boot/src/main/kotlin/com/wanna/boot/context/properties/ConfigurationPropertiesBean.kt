package com.wanna.boot.context.properties

import com.wanna.boot.context.properties.bind.Bindable
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.validation.annotation.Validated
import java.lang.reflect.Method

/**
 * 这是对标注了ConfigurationProperties的Bean去进行的封装
 *
 * @see ConfigurationProperties
 */
open class ConfigurationPropertiesBean(
    private val name: String,
    private val instance: Any?,
    private val bindTarget: Bindable<Any>,
    private val annotation: ConfigurationProperties,
    private val beanType: Class<*>
) {

    /**
     * 绑定方式，是采用Setter的方式去进行注入还是使用构造器的方式去进行注入
     */
    private val bindMethod = BindMethod.forType(beanType)

    /**
     * 获取ConfigurationPropertiesBean的BeanName
     */
    open fun getName() = name

    /**
     * 获取到[Bindable], 用于去进行绑定的目标对象的信息
     *
     * @return Bindable
     */
    fun asTarget(): Bindable<Any> = this.bindTarget

    /**
     * 获取包装的Bean，可以为null；表示运行时在去进行实例化(构造器绑定方式)
     */
    open fun getInstance(): Any? = instance

    /**
     * 获取@ConfigurationProperties注解信息
     *
     * @return @ConfigurationProperties注解
     */
    open fun getAnnotation() = annotation

    /**
     * 获取绑定方式，是采用JavaBean的Setter还是构造器的方式去完成绑定？
     *
     * @return BindMethod(Setter/Constructor)
     */
    open fun getBindMethod() = bindMethod

    /**
     * 获取ConfigurationPropertiesBean的BeanType
     *
     * @return beanType of ConfigurationPropertiesBean
     */
    open fun getBeanType() = this.beanType

    /**
     * 提供一些静态方法去进行ConfigurationPropertiesBean的构建
     */
    companion object {
        /**
         * 根据该Bean的相关信息，从FactoryMethod或者是类上去寻找@ConfigurationProperties注解
         *
         * @param applicationContext applicationContext
         * @param bean bean
         * @param beanName beanName
         * @return 如果找到了@ConfigurationProperties返回Bean；没有找到，那么return null
         */
        @JvmStatic
        fun get(applicationContext: ApplicationContext, bean: Any, beanName: String): ConfigurationPropertiesBean? {
            val factoryMethod = getFactoryMethod(beanName, applicationContext)
            return create(beanName, bean, bean::class.java, factoryMethod)
        }

        /**
         * 为给定的ValueObject去创建ConfigurationPropertiesBean，主要只去解析beanClass当中的@ConfigurationProperties注解；
         * 对于ValueObject，不可能从@Bean的FactoryMethod当中创建，因此FactoryMethod=null；因为运行时才创建对象，因此bean=null；
         *
         * @param beanClass beanClass
         * @param beanName beanName
         * @throws IllegalStateException 如果没有在beanClass上找到@ConfigurationProperties注解的话
         */
        @JvmStatic
        fun forValueObject(beanClass: Class<*>, beanName: String): ConfigurationPropertiesBean {
            return create(beanName, null, beanClass, null)
                ?: throw IllegalStateException("无法在类型上找到合适的@ConfigurationProperties注解去完成ConfigurationPropertiesBean的创建")
        }

        /**
         * 判断该Bean是否是通过FactoryMethod(@Bean方法)被导入进来的？
         *
         * @return 如果是被FactoryMethod导入进来的，那么return FactoryMethod；否则，return null
         */
        private fun getFactoryMethod(beanName: String, applicationContext: ApplicationContext): Method? {
            // fixed: check contains
            if (applicationContext.containsBeanDefinition(beanName) && applicationContext is ConfigurableApplicationContext) {
                val definition = applicationContext.getBeanFactory().getMergedBeanDefinition(beanName)
                if (definition is RootBeanDefinition) {
                    return definition.getResolvedFactoryMethod()
                }
                return null
            }
            return null
        }

        /**
         * 寻找@ConfigurationProperties注解，如果必要的话创建ConfigurationPropertiesBean
         *
         * @param bean bean
         * @param beanName beanName
         * @param factory factoryMethod(@Bean方法注解标注的方法)
         * @return 如果找到了@ConfigurationProperties注解，那么return ConfigurationPropertiesBean；否则return null
         */
        @Suppress("UNCHECKED_CAST")
        private fun create(
            beanName: String, bean: Any?, beanClass: Class<*>, factory: Method?
        ): ConfigurationPropertiesBean? {
            // 从FactoryMethod/beanClass当中去检查@ConfigurationProperties注解
            val annotation = findAnnotation(ConfigurationProperties::class.java, bean, beanClass, factory)
                ?: return null

            // 从FactoryMethod/beanClass当中去找一下@Validated注解
            val validated = findAnnotation(Validated::class.java, bean, beanClass, factory)

            // 构建绑定时, 需要用到的注解信息的列表
            val annotations = if (validated == null) arrayOf(annotation) else arrayOf(annotation, validated)

            // 要去进行绑定的类型, 如果是FactoryMethod的话, 那么使用方法的返回值去进行构建; 如果是类的话, 那么使用beanClass去进行构建
            val bindType =
                if (factory != null) ResolvableType.forMethodReturnType(factory) else ResolvableType.forClass(beanClass)

            // 构建出来Bindable, 去描述要去进行绑定的目标对象的相关信息
            var bindable = Bindable.of<Any>(bindType).withAnnotations(*annotations)

            // 如果已经存在有实例对象了的话, 那么把实例对象, 也去设置到Bindable当中去...
            if (bean != null) {
                bindable = bindable.withExistingValue(bean)
            }
            return ConfigurationPropertiesBean(beanName, bean, bindable, annotation, beanClass)
        }

        /**
         * 从method/type上去找到指定类型的注解
         *
         * @param A 要去寻找的目标泛型
         * @return 找到了return 注解对象；没有找到return null
         */
        private fun <A : Annotation> findAnnotation(
            annotationType: Class<A>, instance: Any?, type: Class<*>, factory: Method?
        ): A? {
            var annotation: A? = null
            // 1.从FactoryMethod当中去寻找该注解
            if (factory != null) {
                annotation = factory.getAnnotation(annotationType)
            }
            // 2.去beanClass当中寻找该注解
            if (annotation == null) {
                annotation = type.getAnnotation(annotationType)
            }
            return annotation
        }
    }

    /**
     * 采用何种方式去进行绑定？JAVA_BEAN/VALUE_OBJECT两种方式
     */
    enum class BindMethod {
        JAVA_BEAN,  // setter for JavaBean
        VALUE_OBJECT;  // Constructor for Value Object(值对象，Kotlin当中就有这个概念)

        companion object {
            /**
             * 如果能从指定的Class当中去找到@ConstructorBinding标注的构造器，那么采用VALUE_OBJECT的方式去绑定；否则采用JAVA_BEAN的setter的方式去进行绑定
             *
             * @return BindMethod for match
             */
            fun forType(type: Class<*>): BindMethod {
                return if (ConfigurationPropertiesBindConstructorProvider.INSTANCE.getBindConstructor(type) == null) JAVA_BEAN
                else VALUE_OBJECT
            }
        }
    }
}