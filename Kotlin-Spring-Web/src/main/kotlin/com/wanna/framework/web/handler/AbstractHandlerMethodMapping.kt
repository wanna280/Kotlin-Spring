package com.wanna.framework.web.handler

import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.core.util.ReflectionUtils
import com.wanna.framework.web.method.HandlerMethod
import com.wanna.framework.web.server.HttpServerRequest
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * 这是一个抽象的HandlerMethod的HandlerMapping，它支持使用HandlerMethod作为HandlerMapping的handler；
 * 它实现了InitializingBean，目的是从容器当中遍历所有的Bean，去初始化MappingRegistry当中的所有的Mapping
 * 对于判断Bean是否是一个Handler，以及如何获取Handler中的HandlerMethod，都作为抽象的模板方法的方式交给子类；
 *
 * 因为它是HandlerMethod的HandlerMapping，因此它应该去处理请求的Handler对象就是HandlerMethod；
 * 也就是说，它会根据path，去找到一个合适的HandlerMethod去包装到HandlerExecutionChain当中
 *
 * @see afterPropertiesSet
 * @see isHandler
 * @see getMappingForMethod
 * @param T Mapping的类型，一般情况下维护的是RequestMapping注解的解析结果RequestMappingInfo(也可以是别的，交给子类去进行完成)
 */
abstract class AbstractHandlerMethodMapping<T> : AbstractHandlerMapping(), InitializingBean {

    // Mapping注册中心，维护了从path -> mapping, name -> mapping, mapping -> MappingRegistration的映射关系
    // 根据path -> mapping -> MappingRegistration，可以获取到path对应的表项当中的具体信息
    private val mappingRegistry = MappingRegistry()

    // HandlerMethod的映射的命名策略(默认为Controller的大写字母#方法名，比如UserController的getUser方法，会被命名为UC#getUser)
    private var namingStrategy: HandlerMethodMappingNamingStrategy<T>? = null

    /**
     * 在初始化HandlerMapping的Bean时，应当从容器当中拿出所有的Bean，去初始化HandlerMethod列表
     *
     * @see initHandlerMethods
     */
    override fun afterPropertiesSet() {
        initHandlerMethods()
    }

    /**
     * 为提供Handler的方式，提供了模板方法；(1)寻找path、(2)根据path去MappingRegistry当中获取到HandlerMethod；
     *
     * @param request request
     * @return 如果找到了合适的HandlerMethod去处理请求，那么return HandlerMethod；如果没有找到，那么return null
     */
    override fun getHandlerInternal(request: HttpServerRequest): Any? {
        // 从request当中去获取到要进行寻找的path
        val lookupPath = initLookupPath(request)

        // 从MappingRegistry当中寻找合适的处理请求的HandlerMethod
        val handlerMethod = lookupHandlerMethod(lookupPath, request)

        // 如果必要的话，在运行时(接收请求时)，需要将HandlerMethod当中的beanName替换为真正的Bean
        return handlerMethod?.createWithResolvedBean()
    }

    /**
     * 遍历所有的Bean，去判断它是否是一个Handler？如果是一个Handler的话，把它所有的HandlerMethod注册到MappingRegistry当中
     *
     * @see detectHandlerMethods
     */
    protected open fun initHandlerMethods() {
        val applicationContext = obtainApplicationContext()
        val bdNames = applicationContext.getBeanDefinitionNames()
        bdNames.forEach {
            val beanType = applicationContext.getType(it)
            // 如果它是一个合格的Handler的话，那么需要探测它内部的HandlerMethod
            if (beanType != null && isHandler(beanType)) {
                detectHandlerMethods(it)
            }
        }
    }

    /**
     * 探测一个Handler上的全部Handler方法，并注册到MappingRegistry当中
     *
     * @param handler beanName or beanObject
     */
    protected open fun detectHandlerMethods(handler: Any) {
        // 如果给定的handler是String，那么从容器当中getType；如果它不是String，那么直接getClass
        val handlerType = if (handler is String) obtainApplicationContext().getType(handler)!! else handler::class.java
        ReflectionUtils.doWithMethods(handlerType) {
            // 交给子类去告诉我，当前的方法是否是一个HandlerMethod？如果return null，则不是；return not null，则是
            val mapping = getMappingForMethod(it, handlerType) ?: return@doWithMethods
            mappingRegistry.registerHandlerMethod(handler, it, mapping)
        }
    }

    /**
     * 从Mapping(例如RequestMappingInfo)当中去获取直接路径，交给子类去进行实现
     *
     * @param mapping
     * @return 从Mapping当中解析到的直接路径列表
     */
    abstract fun getDirectPaths(mapping: T): Set<String>

    /**
     * 从request当中获取到请求的url(不含参数部分)
     *
     * @param request request
     * @return url
     */
    protected open fun initLookupPath(request: HttpServerRequest): String {
        return request.getUrl()
    }

    /**
     * 给定一个BeanClass，去判断它是否是一个Handler？
     *
     * @param beanType beanType
     * @return 它是否是一个Handler？是一个Handler时return true；否则return false
     */
    protected abstract fun isHandler(beanType: Class<*>): Boolean

    /**
     * 根据method和handlerType，去决定Mapping；比如RequestMappingInfo
     *
     * @param method handlerMethod
     * @param handlerType handlerType
     * @return 如果该方法是一个HandlerMethod，return Mapping；如果该方法不是一个HandlerMethod，return null
     */
    protected abstract fun getMappingForMethod(method: Method, handlerType: Class<*>): T?

    /**
     * 寻找到合适的HandlerMethod去处理请求
     *
     * @param lookupPath 要寻找的路径
     * @param request request
     * @return 匹配到的HandlerMethod，如果没有找到的话，return null
     */
    protected open fun lookupHandlerMethod(lookupPath: String, request: HttpServerRequest): HandlerMethod? {
        mappingRegistry.acquireReadLock()
        try {
            // 根据path，直接去获取mapping列表
            val mappings = mappingRegistry.getMappingsByDirectPath(lookupPath)
            val matches = ArrayList<Match>()

            // 交给子类去匹配，哪些Mapping是匹配的？
            addMatchingMappings(matches, request, mappings)

            // 如果没有匹配到合适的结果的话...return null
            if (matches.isEmpty()) {
                return null
            }
            // 获取处理请求的HandlerMethod
            return matches.iterator().next().getHandlerMethod()
        } finally {
            mappingRegistry.releaseReadLock()
        }
    }

    /**
     * 添加匹配到Mapping，遍历所有的Mapping，交给子类去决定该Mapping是否是匹配当前的请求的？
     *
     * @param matches 最终匹配的结果，输出参数
     * @param request request
     * @param mappings 匹配的Mapping列表
     */
    private fun addMatchingMappings(matches: MutableList<Match>, request: HttpServerRequest, mappings: List<T>) {
        for (mapping in mappings) {
            val match = getMatchingMapping(request, mapping)
            if (match != null) {
                val registration = mappingRegistry.getRegistrations()[mapping]
                matches += Match(match, registration!!)
            }
        }
    }

    /**
     * 如何去进行匹配当前请求和Mapping匹配？抽象的模板方法，交给子类去实现
     *
     * @param request request
     * @return 如果匹配到return mapping，如果匹配不到，return null
     */
    abstract fun getMatchingMapping(request: HttpServerRequest, mapping: T): T?

    /**
     * Mapping的注册中心，负责将RequestMapping和HandlerMethod去进行映射；
     * 因为对MappingRegistry的操作涉及到多线程并发的安全问题，这里使用读写锁的方式，去保证并发安全；
     *
     * pathLookup-->根据path去寻找到匹配的Mapping列表
     * nameLookup-->根据name去寻找到List<HandlerMethod>
     * registry-->根据mapping去找到MappingRegistration，供pathLookup去进行使用，因为pathToLookup寻找时，有可能涉及到路径的匹配，需要用到mapping
     */
    inner class MappingRegistry {
        // 操作MappingRegistry的读写锁
        private val readWriteLock = ReentrantReadWriteLock()

        // 根据path去进行寻找，value-RequestMappingInfo(@RequestMapping注解的相关信息)
        private val pathLookup = ConcurrentHashMap<String, MutableList<T>>()

        // 根据name去进行寻找到合适的HandlerMethod的Map(key-name,value-HandlerMethod List)
        private val nameLookup = ConcurrentHashMap<String, MutableList<HandlerMethod>>()

        // MappingRegistry(key-mapping,value-MappingRegistration)
        private val registry: MutableMap<T, MappingRegistration<T>> = LinkedHashMap()

        /**
         * 获取MappingRegistry的读锁
         */
        fun acquireReadLock() {
            this.readWriteLock.readLock().lock()
        }

        /**
         * 释放MappingRegistry的读锁
         */
        fun releaseReadLock() {
            this.readWriteLock.readLock().unlock()
        }

        /**
         * 获取注册中心当中全部已经注册的表项(mapping->MappingRegistration)
         */
        fun getRegistrations(): Map<T, MappingRegistration<T>> {
            return this.registry
        }

        /**
         * 根据直接路径去获取到注册的Mapping
         */
        fun getMappingsByDirectPath(lookupPath: String): List<T> {
            return pathLookup[lookupPath] ?: emptyList()
        }

        /**
         * 往MappingRegistry当中注册一个HandlerMethod，需要涉及到MappingRegistry的写，需要获取写锁
         *
         * @param handler handler(beanName or beanObject)
         * @param mapping mapping
         * @param method handlerMethod
         */
        fun registerHandlerMethod(handler: Any, method: Method, mapping: T) {
            this.readWriteLock.writeLock().lock()  // acquireWriteLock
            try {
                // 根据handler和method去创建HandlerMethod
                val handlerMethod = createHandlerMethod(handler, method)

                // 从子类当中去获取直接路径，并将path->mapping的映射注册到MappingRegistry当中
                val paths = getDirectPaths(mapping)
                paths.forEach { addPathLookup(it, mapping) }

                var mappingName: String? = null
                val namingStrategy = getHandlerMethodMappingNamingStrategy()

                // 如果有命名策略的话，那么需要生成name并去注册
                if (namingStrategy != null) {
                    mappingName = namingStrategy.getName(handlerMethod, mapping)
                    addNameToLookup(mappingName, handlerMethod)
                }

                // 将Mapping作为key，注册到registry当中
                this.registry[mapping] = MappingRegistration(mapping, paths, handlerMethod, mappingName)
            } finally {
                this.readWriteLock.writeLock().unlock()  // releaseWriteLock
            }
        }

        /**
         * 将name->List<HandlerMethod>注册到MappingRegistry当中
         */
        private fun addNameToLookup(name: String, handlerMethod: HandlerMethod) {
            var handlerMethods = this.nameLookup[name]
            if (handlerMethods == null) {
                handlerMethods = ArrayList()
            }
            handlerMethods.forEach {
                if (it == handlerMethod) {
                    return
                }
            }
            // copy一份，去进行添加，使用CopyOnWrite机制去进行实现
            val newHandlerMethods = ArrayList<HandlerMethod>(handlerMethods)
            newHandlerMethods += handlerMethod
            this.nameLookup[name] = newHandlerMethods
        }

        /**
         * 将path->mapping，注册到MappingRegistry当中
         *
         * @param path path
         * @param mapping mapping
         */
        private fun addPathLookup(path: String, mapping: T) {
            this.pathLookup.putIfAbsent(path, ArrayList())
            this.pathLookup[path]!! += mapping
        }

        /**
         * 创建HandlerMethod，如果handler is String，说明它是beanName，需要后期再从容器getBean；
         * 如果它不是String类型，说明它就是一个真正的Bean，后期不必再去进行解析了，直接去构建HandlerMethod即可
         *
         * @param handler handler(beanName or beanObject)
         * @param method handlerMethod
         * @return 构建好的HandlerMethod
         */
        private fun createHandlerMethod(handler: Any, method: Method): HandlerMethod {
            if (handler !is String) {
                return HandlerMethod.newHandlerMethod(handler, method)
            }

            // 基于beanName、beanFactory、method去进行构建HandlerMethod
            val beanFactory = obtainApplicationContext().getAutowireCapableBeanFactory()
            return HandlerMethod.newHandlerMethod(beanFactory, handler, method)
        }
    }

    /**
     * Mapping注册的表项，封装了各种信息
     *
     * @param mapping mapping
     * @param directPaths 路径列表
     * @param handlerMethod handlerMethod(bean+method)
     * @param name mappingName
     */
    class MappingRegistration<T>(
        val mapping: T, val directPaths: Set<String>, val handlerMethod: HandlerMethod, val name: String?
    )

    /**
     * 封装请求的匹配的结果
     *
     * @param mapping mapping
     * @param registration MappingRegistration
     */
    inner class Match(val mapping: T, val registration: MappingRegistration<T>) {
        fun getHandlerMethod() = registration.handlerMethod
        fun getDirectPaths() = registration.directPaths
    }

    open fun setHandlerMethodMappingNamingStrategy(namingStrategy: HandlerMethodMappingNamingStrategy<T>) {
        this.namingStrategy = namingStrategy
    }

    open fun getHandlerMethodMappingNamingStrategy(): HandlerMethodMappingNamingStrategy<T>? {
        return this.namingStrategy
    }
}