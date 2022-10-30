package com.wanna.boot.actuate.web.mappings.mvc

import com.wanna.boot.actuate.web.mappings.MappingDescriptionProvider
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.web.DispatcherHandler
import com.wanna.framework.web.HandlerMapping
import com.wanna.framework.web.method.HandlerMethod
import com.wanna.framework.web.method.RequestMappingInfo
import com.wanna.framework.web.method.RequestMappingInfoHandlerMapping

/**
 * 提供对于Mvc当中的Mapping去进行描述的Provider
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/30
 */
open class MvcMappingDescriptionProvider : MappingDescriptionProvider {

    companion object {

        /**
         * 所有的去对HandlerMapping去进行描述的Provider
         */
        @JvmStatic
        private var descriptionProviders: List<HandlerMappingDescriptionProvider<*>> =
            listOf(
                RequestMappingInfoHandlerMappingDescriptionProvider()
            )

        /**
         * 针对给定的[HandlerMapping]，尝试使用所有的[HandlerMappingDescriptionProvider]去进行策略的探测
         *
         * @param handlerMapping handlerMapping
         * @param descriptionProviders 描述HandlerMapping的Provider
         * @return 该HandlerMapping当中探测到的所有的[DispatcherHandlerMappingDescription]
         */
        @JvmStatic
        private fun <T : HandlerMapping> describe(
            handlerMapping: T,
            descriptionProviders: List<HandlerMappingDescriptionProvider<*>>
        ): List<DispatcherHandlerMappingDescription> {
            descriptionProviders.forEach {
                // 如果类型匹配的话，那么就交给它(HandlerMappingDescriptionProvider)去进行处理
                if (ClassUtils.isAssignFrom(it.getMappingClass(), handlerMapping::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return (it as HandlerMappingDescriptionProvider<T>).describe(handlerMapping)
                }
            }
            return emptyList()
        }
    }


    override fun getMappingName(): String = "mvc"

    /**
     * 对一个[ApplicationContext]当中的所有的[DispatcherHandler]当中的所有的[HandlerMapping]去进行探测
     *
     * @param applicationContext 待探测的[ApplicationContext]
     * @return 探测得到的结果(Key是DispatchHandler的name, Value是该DispatcherHandler当中的所有的HandlerMapping的描述结果)
     */
    override fun describeMappings(applicationContext: ApplicationContext): Map<String, List<DispatcherHandlerMappingDescription>> {
        val mappings = LinkedHashMap<String, List<DispatcherHandlerMappingDescription>>()
        determineDispatcherHandlers(applicationContext)
            .forEach { (name, handler) ->
                val handlerMappings = handler.getHandlerMappings()
                mappings[name] = if (handlerMappings != null) describeMappings(handlerMappings) else emptyList()
            }
        return mappings
    }

    /**
     * 对于给定的HandlerMapping列表，去进行一一探测，并进行merge得到所有的[DispatcherHandlerMappingDescription]对象
     *
     * @param mappings 需要去进行探测的HandlerMappings
     * @return DispatcherHandlerMappingDescriptions
     */
    private fun <T : HandlerMapping> describeMappings(mappings: List<T>): List<DispatcherHandlerMappingDescription> {
        return mappings
            .map { describe(it, descriptionProviders) }
            .flatMap { it.toList() }
            .toList()
    }


    /**
     * 从ApplicationContext当中去探测到所有的[DispatcherHandler]
     *
     * @param applicationContext 待探测的ApplicationContext
     * @return 从ApplicationContext当中去探测到的DispatcherHandler列表
     */
    private fun determineDispatcherHandlers(applicationContext: ApplicationContext): Map<String, DispatcherHandler> {
        return applicationContext.getBeansForType(DispatcherHandler::class.java)
    }

    /**
     * 针对一种类型的[HandlerMapping]去进行描述的Provider，我们根据目前已经有的[HandlerMapping]类型去进行枚举提供实现
     *
     * @param T 支持去进行描述的HandlerMapping的具体类型
     */
    interface HandlerMappingDescriptionProvider<T> {

        /**
         * 获取支持去进行处理的HandlerMapping的类型
         *
         * @return handlerMapping Class
         */
        fun getMappingClass(): Class<T>

        /**
         * 对于给定的[HandlerMapping]去执行真正的描述
         *
         * @param handlerMapping 待探测的HandlerMapping
         * @return 对该HandlerMapping去得到的描述结果
         */
        fun describe(handlerMapping: T): List<DispatcherHandlerMappingDescription>
    }

    /**
     * 对于[RequestMappingInfoHandlerMapping]去进行描述的Provider
     */
    private class RequestMappingInfoHandlerMappingDescriptionProvider :
        HandlerMappingDescriptionProvider<RequestMappingInfoHandlerMapping> {

        /**
         * 获取需要去进行处理的HandlerMapping Class
         *
         * @return RequestMappingInfoHandlerMapping
         */
        override fun getMappingClass() = RequestMappingInfoHandlerMapping::class.java

        /**
         * 对于一个[RequestMappingInfoHandlerMapping]去进行描述
         *
         * @param handlerMapping HandlerMapping
         * @return 对该HandlerMapping去进行描述，得到的描述信息列表
         */
        override fun describe(handlerMapping: RequestMappingInfoHandlerMapping): List<DispatcherHandlerMappingDescription> {
            // 从HandlerMapping当中，获取到所有的HandlerMethods
            val handlerMethods = handlerMapping.getHandlerMethods()

            // 对所有的HandlerMethod去进行描述
            return handlerMethods.map { this.describe(it.key, it.value) }.toList()
        }

        /**
         * 对于一个RequestMappingInfo和一个HandlerMethod去进行描述
         *
         * @param requestMappingInfo RequestMappingInfo
         * @param handler HandlerMethod
         * @return 对于一个[HandlerMethod]所处理的[RequestMappingInfo]信息去进行描述的结果
         */
        private fun describe(
            requestMappingInfo: RequestMappingInfo,
            handler: HandlerMethod
        ): DispatcherHandlerMappingDescription {
            val details = DispatcherHandlerMappingDetails()
            details.handlerMethod = HandlerMethodDescription(handler)
            details.requestMappingConditions = RequestMappingConditionsDescription(requestMappingInfo)

            // 第一个参数handler, 第二个参数predicate, 第三个参数Details
            return DispatcherHandlerMappingDescription(handler.toString(), requestMappingInfo.toString(), details)
        }
    }
}