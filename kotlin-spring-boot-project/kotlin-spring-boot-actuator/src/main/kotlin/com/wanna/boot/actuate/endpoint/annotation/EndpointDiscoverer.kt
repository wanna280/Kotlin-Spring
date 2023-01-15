package com.wanna.boot.actuate.endpoint.annotation

import com.wanna.boot.actuate.endpoint.*
import com.wanna.boot.actuate.endpoint.invoke.OperationInvoker
import com.wanna.boot.actuate.endpoint.invoke.reflect.OperationMethod
import com.wanna.boot.actuate.endpoint.invoke.reflect.ReflectiveOperationInvoker
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.util.ReflectionUtils
import java.util.function.Supplier

/**
 * Endpoint的Discovery, 负责提供SpringBeanFactory当中的标注了@Endpoint注解的Bean的匹配, 并暴露给使用则
 *
 * @param applicationContext ApplicationContext, 要去寻找EndpointBean的ApplicationContext
 */
abstract class EndpointDiscoverer<E : ExposableEndpoint<O>, O : Operation>(private val applicationContext: ApplicationContext) :
    EndpointsSupplier<E> {

    companion object {
        private val OPERATION_TYPES: Map<OperationType, Class<out Annotation>> = mapOf(
            OperationType.READ to ReadOperation::class.java,
            OperationType.WRITE to WriteOperation::class.java,
            OperationType.DELETE to DeleteOperation::class.java
        )

        private val OPERATION_TYPES_REVERSE: Map<Class<out Annotation>, OperationType> = mapOf(
            ReadOperation::class.java to OperationType.READ,
            WriteOperation::class.java to OperationType.WRITE,
            DeleteOperation::class.java to OperationType.DELETE
        )
    }

    private var endpoints: Collection<E>? = null

    /**
     * 获取要扫描到的所有的EndpointBean的列表
     *
     * @return 从SpringBeanFactory当中扫描到的所有的Endpoint列表
     */
    override fun getEndpoints(): Collection<E> {
        var endpoints = this.endpoints
        if (endpoints == null) {
            endpoints = discoveryEndpoints()
            this.endpoints = endpoints
        }
        return endpoints
    }

    /**
     * 从SpringBeanFactory当中去发现所有的Endpoints
     *
     * @return 发现的所有的Endpoint列表
     */
    private fun discoveryEndpoints(): Collection<E> {

        // 创建EndpointBean
        val endpointBeans = createEndpointBeans()

        // 将EndpointBean转换为Endpoint
        return endpointBeans.mapNotNull { endpointBean ->
            val beanType = endpointBean.beanType
            // 如果不应该去进行暴露, 那么return null
            if (!isEndpointTypeExposed(beanType)) {
                null
            } else {
                val operations = ArrayList<O>()
                // 获取一个类上的所有的方法, 去进行Operation的匹配
                ReflectionUtils.doWithMethods(beanType) { method ->
                    OPERATION_TYPES.values.mapNotNull { AnnotatedElementUtils.getMergedAnnotation(method, it) }
                        .map {
                            val operationMethod =
                                OperationMethod(OPERATION_TYPES_REVERSE[it.annotationClass.java]!!, method)
                            createOperation(
                                endpointBean.getId(),
                                operationMethod,
                                ReflectiveOperationInvoker(endpointBean.getBean(), operationMethod)
                            )
                        }.forEach(operations::add)
                }
                // 根据EndpointBean和Operation列表, 去转换成为Endpoint
                createEndpoint(endpointBean.getId(), endpointBean.getBean(), operations)
            }
        }.toList()
    }

    /**
     * 创建一个Endpoint的逻辑, 因为泛型的类型是交给子类去进行决定的, 因此创建的逻辑, 也得交给子类去进行实现
     *
     * @param endpointBean endpointBeanObject
     * @param id endpointId
     * @param operations 该Endpoint当中的Operation列表
     */
    protected abstract fun createEndpoint(id: EndpointId, endpointBean: Any, operations: Collection<O>): E

    /**
     * 创建一个Endpoint当中的Operation的逻辑, 因为泛型O的类型是交给智略去进行决定的, 因此创建的逻辑, 也得交给子类去进行实现
     *
     * @param endpointId endpointId
     * @param operationMethod OperationType and Method
     * @param invoker invoker of OperationMethod
     * @return 根据给定的相关信息, 去创建好的Operation
     */
    protected abstract fun createOperation(
        endpointId: EndpointId,
        operationMethod: OperationMethod,
        invoker: OperationInvoker
    ): O

    /**
     * 是否该Endpoint的类型需要去进行暴露？(默认情况下, 只要有@Endpoint注解就行, 支持去进行自定义)
     *
     * @param beanType beanType
     * @return 如果需要暴露, return true; 否则return false
     */
    protected open fun isEndpointTypeExposed(beanType: Class<*>): Boolean {
        return true
    }

    /**
     * 创建所有的EndpointBean, 遍历所有的BeanFactory当中的所有的BeanDefinition, 去检查@Endpoint注解,
     * 如果该Bean的beanClass上标注了@Endpoint注解, 那么需要将它封装成一个EndpointBean
     *
     * @return 从BeanFactory当中找到的所有的EndpointBean列表
     */
    private fun createEndpointBeans(): Collection<EndpointBean> {
        val endpointBeans = HashMap<EndpointId, EndpointBean>()
        val beanFactory = (applicationContext as ConfigurableApplicationContext).getBeanFactory()
        for (beanName in beanFactory.getBeanDefinitionNames()) {
            val definition = beanFactory.getMergedBeanDefinition(beanName) as RootBeanDefinition

            // 1.尝试获取beanClass, 如果没有beanClass, 那么尝试从@Bean方法的返回值上去进行获取
            var beanClass: Class<*>? = null
            if (definition.hasBeanClass()) {
                beanClass = definition.getBeanClass()
            }
            // 2.如果beanClass确实是存在的话, 那么解析一下beanClassName
            if (definition.getBeanClassName() != null) {
                beanClass = definition.resolveBeanClass(beanFactory.getBeanClassLoader())
            }

            // 3.再检查一下@Bean方法的返回值
            if (beanClass == null && definition.getResolvedFactoryMethod() != null) {
                beanClass = definition.getResolvedFactoryMethod()?.returnType
            }

            // 如果beanClass==null或者beanClass上没有Endpoint注解, 那么直接pass掉
            if (beanClass == null || !AnnotatedElementUtils.isAnnotated(beanClass, Endpoint::class.java)) {
                continue
            }

            // 创建EndpointBean, 并加入到结果的列表当中
            val endpointBean = createEndpointBean(beanName)
            endpointBeans.putIfAbsent(endpointBean.getId(), endpointBean)
        }
        return endpointBeans.values
    }

    /**
     * 创建一个EndpointBean
     *
     * @param beanName beanName
     * @return 根据该beanName的相关信息, 去构建好的EndpointBean
     */
    private fun createEndpointBean(beanName: String): EndpointBean {
        return EndpointBean(
            applicationContext.getEnvironment(),
            beanName,
            applicationContext.getType(beanName)!!
        ) { applicationContext.getBean(beanName) }
    }

    /**
     * 标注了@Endpoint的一个SpringBean的相关信息
     *
     * @param environment environment
     * @param beanName beanName
     * @param beanSupplier beanInstanceSupplier
     * @param beanType beanType
     */
    class EndpointBean(
        val environment: Environment,
        val beanName: String,
        val beanType: Class<*>,
        val beanSupplier: Supplier<Any>
    ) {

        private var id: EndpointId? = null

        init {
            id = EndpointId.of(
                environment,
                AnnotatedElementUtils.getMergedAnnotation(beanType, Endpoint::class.java)!!.id
            )
        }

        fun getBean(): Any = beanSupplier.get()

        fun getId(): EndpointId = id!!
    }
}