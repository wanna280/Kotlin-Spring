package com.wanna.boot.autoconfigure

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.GenericBeanDefinition
import com.wanna.framework.context.annotation.ImportBeanDefinitionRegistrar
import com.wanna.framework.core.type.AnnotationMetadata
import java.util.function.Supplier

/**
 * 这是一个给SpringBoot当中去进行自动配置包的类，这个类的主要作用是，往容器当中注册一个BasePackages组件，
 * 后期可以从容器当中获取到这个组件从而获取到自动配置的包的列表
 *
 * @see AutoConfigurationPackage
 */
class AutoConfigurationPackages {
    companion object {
        // 自动配置包Bean的beanName
        private val AUTO_CONFIGURATION_PACKAGES_BEAN = AutoConfigurationPackages::class.java.name

        /**
         * 注册一个配置包的BeanDefinition到容器当中
         *
         * @param registry BeanDefinitionRegistry
         * @param packages 要自动配置的包的列表
         */
        @JvmStatic
        private fun register(registry: BeanDefinitionRegistry, packages: Array<String>) {
            if (registry.containsBeanDefinition(AUTO_CONFIGURATION_PACKAGES_BEAN)) {
                val basePackagesBeanDefinition =
                    registry.getBeanDefinition(AUTO_CONFIGURATION_PACKAGES_BEAN) as BasePackagesBeanDefinition
                basePackagesBeanDefinition.addPackages(packages)
            } else {
                registry.registerBeanDefinition(AUTO_CONFIGURATION_PACKAGES_BEAN, BasePackagesBeanDefinition(packages))
            }
        }
    }


    /**
     * 这是一个BeanDefinitionRegistrar，负责给容器中导入BasePackagesBeanDefinition，从而实现往容器当中注册一个BasePackages对象；
     * 在BasePackages对象当中维护了当前SpringApplication要进行自动配置的包的列表
     *
     * @see BasePackagesBeanDefinition
     * @see BasePackages
     */
    class Registrar : ImportBeanDefinitionRegistrar {
        override fun registerBeanDefinitions(annotationMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
            register(registry, PackageImports(annotationMetadata).getPackageNames())
        }
    }

    /**
     * BasePackages的Bean，会被SpringBoot注册到容器当中
     *
     * @see BasePackagesBeanDefinition
     */
    class BasePackages(basePackages: Array<String>) {
        val basePackages = basePackages.toMutableList()
    }

    /**
     * 这是一个维护BasePackages的自定义BeanDefinition，它负责使用InstanceSupplier的方式将BasePackages对象注册到容器当中
     *
     * @see BasePackages
     */
    class BasePackagesBeanDefinition(basePackages: Array<String>) : GenericBeanDefinition() {
        private val basePackages = LinkedHashSet<String>()

        fun addPackages(packages: Array<String>) {
            basePackages += packages
        }

        init {
            setBeanClass(BasePackages::class.java)
            setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
            addPackages(basePackages)
        }

        /**
         * 获取实例化对象的Supplier，BasePackages对象，在Spring容器启动的过程当中，会被自动使用Supplier的方式去完成实例化
         *
         * @see BeanDefinition.getInstanceSupplier
         */
        override fun getInstanceSupplier(): Supplier<*> {
            return Supplier { BasePackages(this@BasePackagesBeanDefinition.basePackages.toTypedArray()) }
        }
    }

    /**
     * 这是一个负责根据注解(AutoConfigurationPackage)信息去解析要配置的包的相关信息
     */
    @Suppress("UNCHECKED_CAST")
    class PackageImports(annotationMetadata: AnnotationMetadata) {
        private val packages = LinkedHashSet<String>()

        init {
            val attributes = annotationMetadata.getAnnotationAttributes(AutoConfigurationPackage::class.java)
            packages += (attributes["basePackages"] as Array<String>)
            packages += (attributes["basePackageClasses"] as Array<Class<*>>).map { it.packageName }.toList()
            if (packages.isEmpty()) {
                packages += annotationMetadata.getClassName()
            }
        }

        /**
         * 获取之前解析好的要导入的包的packages列表
         */
        fun getPackageNames(): Array<String> {
            return packages.toTypedArray()
        }
    }


}