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
import com.wanna.framework.core.util.ReflectionUtils
import java.lang.reflect.Method
import java.util.function.Supplier

/**
 * Endpoint的Discovery
 */
abstract class EndpointDiscoverer<E : ExposableEndpoint<O>, O : Operation>(val applicationContext: ApplicationContext) :
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

    override fun getEndpoints(): Collection<E> {
        if (endpoints == null) {
            this.endpoints = discoveryEndpoints()
        }
        return endpoints!!
    }

    /**
     * 发现所有的Endpoints
     */
    private fun discoveryEndpoints(): Collection<E> {
        val endpointBeans = createEndpointBeans()
        return endpointBeans.map { endpointBean ->
            val beanType = endpointBean.beanType
            val operations = ArrayList<O>()
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
            createEndpoint(endpointBean.getId(), endpointBean.getBean(), operations)
        }.toList()
    }

    protected abstract fun createEndpoint(id: EndpointId, endpointBean: Any, operations: Collection<O>): E

    protected abstract fun createOperation(id: EndpointId, method: OperationMethod, invoker: OperationInvoker): O

    private fun createEndpointBeans(): Collection<EndpointBean> {
        val endpointBeans = HashMap<EndpointId, EndpointBean>()
        val beanFactory = (applicationContext as ConfigurableApplicationContext).getBeanFactory()
        val beanDefinitionNames = beanFactory.getBeanDefinitionNames()
        beanDefinitionNames.forEach { beanName ->
            val definition = beanFactory.getMergedBeanDefinition(beanName) as RootBeanDefinition
            var beanClass: Class<*>? = null
            if (definition.getBeanClass() != null) {
                beanClass = definition.getBeanClass()
            }
            if (beanClass == null && definition.getResolvedFactoryMethod() != null) {
                beanClass = definition.getResolvedFactoryMethod()!!.returnType
            }
            AnnotatedElementUtils.getMergedAnnotation(beanClass!!, Endpoint::class.java) ?: return@forEach
            val endpointBean = createEndpointBean(beanName)
            endpointBeans.putIfAbsent(endpointBean.getId(), endpointBean)
        }
        return endpointBeans.values
    }

    /**
     * 创建一个EndpointBean
     *
     * @param beanName beanName
     */
    private fun createEndpointBean(beanName: String): EndpointBean {
        return EndpointBean(
            applicationContext.getEnvironment(),
            beanName,
            applicationContext.getType(beanName)!!
        ) { applicationContext.getBean(beanName) }
    }

    /**
     * 标注了@Endpoint的Bean
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