package com.wanna.framework.web.handler

import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.LinkedMultiValueMap
import com.wanna.framework.util.ReflectionUtils
import com.wanna.framework.web.HandlerMapping
import com.wanna.framework.web.cors.CorsConfiguration
import com.wanna.framework.web.method.HandlerMethod
import com.wanna.framework.web.server.HttpServerRequest
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * 这是一个抽象的HandlerMethod的HandlerMapping，它支持使用HandlerMethod作为HandlerMapping的handler；
 *
 * * 1.它实现了InitializingBean，目的是从容器当中遍历所有的Bean，去初始化MappingRegistry当中的所有的Mapping
 * * 2.对于判断Bean是否是一个Handler，以及如何获取Handler中的HandlerMethod，都作为抽象的模板方法的方式交给子类；
 * * 3.因为它是HandlerMethod的HandlerMapping，因此它应该去处理请求的Handler对象就是HandlerMethod；
 * 也就是说，它会根据path，去找到一个合适的HandlerMethod去包装到HandlerExecutionChain当中；
 * * 4.扩展了父类当中对于Cors的功能，对Cors提供类级别的控制以及方法级别的控制(@CrossOrigin)
 *
 * @see afterPropertiesSet
 * @see isHandler
 * @see getMappingForMethod
 * @param T Mapping的类型，一般情况下维护的是RequestMapping注解的解析结果RequestMappingInfo(也可以是别的，交给子类去进行完成)
 */
abstract class AbstractHandlerMethodMapping<T> : AbstractHandlerMapping(), InitializingBean {

    /**
     * Mapping注册中心，维护了从path -> mapping, name -> mapping, mapping -> MappingRegistration的映射关系；
     * 根据path -> mapping -> MappingRegistration，可以获取到path对应的表项当中的具体信息
     */
    protected val mappingRegistry = MappingRegistry()

    /**
     * HandlerMethod的映射的命名策略(默认为Controller的大写字母#方法名，比如UserController的getUser方法，会被命名为UC#getUser)
     */
    @Nullable
    private var namingStrategy: HandlerMethodMappingNamingStrategy<T>? = null


    /**
     * 在初始化[HandlerMapping]对象时，需要从SpringBeanFactory当中拿出所有的Bean去进行探测，
     * 去初始化[HandlerMethod]列表，方便后期处理请求时，可以快速找到对应的HandlerMethod去处理请求
     *
     * @see initHandlerMethods
     */
    override fun afterPropertiesSet() {
        initHandlerMethods()
    }

    /**
     * 获取当前[AbstractHandlerMapping]当中的[MappingRegistry]所有的已经完成注册的HandlerMethods
     *
     * @return HandlerMethods(Key-Mapping, Value-HandlerMethod), Key最典型的是RequestMappingInfo
     */
    open fun getHandlerMethods(): Map<T, HandlerMethod> {
        this.mappingRegistry.acquireReadLock()
        try {
            // 从MappingRegistry当中拿出来所有的Registration注册表项，并转换成为<Mapping, HandlerMethod>的Map
            return mappingRegistry.getRegistrations().map { it.key to it.value.handlerMethod }.toMap()
        } finally {
            this.mappingRegistry.releaseReadLock()
        }
    }

    /**
     * 为提供Handler的方式，提供了模板方法；(1)寻找path、(2)根据path去MappingRegistry当中获取到HandlerMethod；
     *
     * @param request request
     * @return 如果找到了合适的HandlerMethod去处理请求，那么return HandlerMethod；如果没有找到，那么return null
     */
    @Nullable
    override fun getHandlerInternal(request: HttpServerRequest): Any? {
        // 从request当中去获取到要进行寻找的path
        val lookupPath = initLookupPath(request)
        this.mappingRegistry.acquireReadLock()  // acquire read lock
        try {
            // 从MappingRegistry当中寻找合适的处理请求的HandlerMethod
            val handlerMethod = lookupHandlerMethod(lookupPath, request)

            // 如果必要的话，在运行时(接收请求时)，需要将HandlerMethod当中的beanName替换为真正的Bean
            return handlerMethod?.createWithResolvedBean()
        } finally {
            this.mappingRegistry.releaseReadLock()  // release read lock
        }

    }

    /**
     * 遍历所有的Bean，去判断它是否是一个Handler？
     * 如果是一个Handler的话，把它所有的HandlerMethod注册到MappingRegistry当中
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
            registerHandlerMethod(handler, it, mapping)
        }
    }

    /**
     * 创建HandlerMethod，如果handler is String，说明它是beanName，需要后期再从容器getBean；
     * 如果它不是String类型，说明它就是一个真正的Bean，后期不必再去进行解析了，直接去构建HandlerMethod即可
     *
     * @param handler handler(beanName or beanObject)
     * @param method handlerMethod
     * @return 构建好的HandlerMethod
     */
    protected open fun createHandlerMethod(handler: Any, method: Method): HandlerMethod {
        if (handler !is String) {
            return HandlerMethod.newHandlerMethod(handler, method)
        }

        // 基于beanName、beanFactory、method去进行构建HandlerMethod
        val beanFactory = obtainApplicationContext().getAutowireCapableBeanFactory()
        return HandlerMethod.newHandlerMethod(beanFactory, handler, method)
    }

    /**
     * 注册一个HandlerMethod到MappingRegistry当中
     *
     * @param handler handler
     * @param method method
     * @param mapping mapping
     */
    protected open fun registerHandlerMethod(handler: Any, method: Method, mapping: T) {
        mappingRegistry.registerHandlerMethod(handler, method, mapping)
    }

    /**
     * 从Mapping(例如RequestMappingInfo)当中去获取直接路径，交给子类去进行实现
     *
     * @param mapping Mapping
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
     * 给定一个BeanClass，去判断它是否是一个Handler？(最典型的是@Controller注解)
     *
     * @param beanType beanType
     * @return 它是否是一个Handler？是一个Handler时return true；否则return false
     */
    protected abstract fun isHandler(beanType: Class<*>): Boolean

    /**
     * 根据method和handlerType，去构建出来合适的Mapping(比如RequestMappingInfo)
     *
     * @param method handlerMethod
     * @param handlerType handlerType
     * @return 如果该方法是一个HandlerMethod，return Mapping；如果该方法不是一个HandlerMethod，return null
     */
    @Nullable
    protected abstract fun getMappingForMethod(method: Method, handlerType: Class<*>): T?

    /**
     * 寻找到合适的HandlerMethod去处理请求
     *
     * @param lookupPath 要寻找的路径
     * @param request request
     * @return 根据给定的路径去匹配到的HandlerMethod，如果没有找到的话，return null
     */
    @Nullable
    protected open fun lookupHandlerMethod(lookupPath: String, request: HttpServerRequest): HandlerMethod? {
        mappingRegistry.acquireReadLock()   // acquire read Lock
        try {
            val matches = ArrayList<Match>()

            // 根据path，直接去获取mapping列表
            val directPathMatches = mappingRegistry.getMappingsByDirectPath(lookupPath)

            // 如果根据直接路径就匹配到了合适的Mapping，那么交给子类去匹配，哪些Mapping是匹配的？
            if (directPathMatches.isNotEmpty()) {
                addMatchingMappings(matches, request, directPathMatches)
            }

            // 如果根据直接路径没有匹配到合适的Mapping，那么遍历所有的Mapping去进行匹配...
            // 挨个去进行path/headers/params的匹配，如果找到了合适的匹配结果，将结果放入到matches当中
            if (matches.isEmpty()) {
                addMatchingMappings(matches, request, mappingRegistry.getRegistrations().keys)
            }

            // 如果没有匹配到合适的结果的话...return null
            if (matches.isEmpty()) {
                return handleNoMatch(directPathMatches.toSet(), lookupPath, request)
            }
            val bestMatch = matches.iterator().next()
            handleMatch(bestMatch.mapping, lookupPath, request)
            // 获取处理请求的HandlerMethod
            return bestMatch.getHandlerMethod()
        } finally {
            mappingRegistry.releaseReadLock()  // release readLock
        }
    }

    /**
     * 如果没有找到合适的HandlerMethod去处理当前请求的话，有可能需要提供别的渠道去获取HandlerMethod作为fallback；
     * 对于想要提供fallback的情况，就可以在这里去进行编写自定义的逻辑，去进行更多的自定义工作
     *
     * @param mappings mappings
     * @param lookupPath 要去进行寻找的路径
     * @param request request
     * @return 需要使用的fallback的HandlerMethod
     */
    protected open fun handleNoMatch(mappings: Set<T>, lookupPath: String, request: HttpServerRequest): HandlerMethod? {
        return null
    }

    /**
     * 在找到了合适的Handler去处理本次请求之后，有可能需要去进行扩展，因此，需要留出来足够的模板方法
     *
     * @param mapping mapping
     * @param lookupPath lookupPath
     * @param request request
     */
    protected open fun handleMatch(mapping: T, lookupPath: String, request: HttpServerRequest) {
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, lookupPath)
    }

    /**
     * 是否有CorsConfiguration？我们要去扩展父类的是否有CorsConfigurationSource的逻辑，
     * 因为我们有针对具体的HandlerMethod去配置具体的CorsConfiguration，我们这里需要去进行检查MappingRegistry
     *
     * @param handler handler
     * @return 如果MappingRegistry当中有CorsConfiguration，那么return true
     */
    override fun hasCorsConfigurationSource(handler: Any): Boolean {
        return super.hasCorsConfigurationSource(handler)
                || (handler is HandlerMethod && this.mappingRegistry.getCorsConfiguration(handler) != null)
    }

    /**
     * 针对指定的HandlerMethod，如果必要的话，去进行初始化CorsConfiguration
     *
     * @param handler handler
     * @param mapping mapping
     * @param method method
     * @return CorsConfiguration
     */
    @Nullable
    protected open fun initCorsConfiguration(handler: Any, method: Method, mapping: T): CorsConfiguration? = null

    /**
     * 我们这是针对HandlerMethod的HandlerMapping，因此对于获取CorsConfiguration的逻辑我们应该去进行扩展，
     * 如果之前已经有过CorsConfig(比如Handler本身就是CorsConfigurationSource)，
     * 现在的HandlerMethod上也有CorsConfig，那么我们需要去进行扩展(combine)并构建一个合适的CorsConfiguration去进行返回
     *
     * @param request request
     * @param handler handler
     * @return CorsConfiguration
     */
    @Nullable
    override fun getCorsConfiguration(request: HttpServerRequest, handler: Any): CorsConfiguration? {
        var corsConfig = super.getCorsConfiguration(request, handler)
        if (handler is HandlerMethod) {
            val corsConfigFromMethod = this.mappingRegistry.getCorsConfiguration(handler)
            corsConfig = corsConfig?.combine(corsConfigFromMethod) ?: corsConfigFromMethod
        }
        return corsConfig
    }

    /**
     * 添加匹配到Mapping，遍历所有的Mapping，交给子类去决定该Mapping是否是匹配当前的请求的？
     *
     * @param matches 最终匹配的结果，输出参数
     * @param request request
     * @param mappings 匹配的Mapping列表
     */
    private fun addMatchingMappings(matches: MutableList<Match>, request: HttpServerRequest, mappings: Collection<T>) {
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
        /**
         * 操作MappingRegistry的读写锁
         */
        private val readWriteLock = ReentrantReadWriteLock()

        /**
         * 根据path去进行寻找，value-RequestMappingInfo(@RequestMapping注解的相关信息)
         */
        private val pathLookup = LinkedMultiValueMap<String, T>()

        /**
         * 根据name去进行寻找到合适的HandlerMethod的Map(Key-name,Value-HandlerMethod List)
         */
        private val nameLookup = ConcurrentHashMap<String, MutableList<HandlerMethod>>()

        /**
         * MappingRegistry(Key-Mapping, Value-MappingRegistration)
         */
        private val registry: MutableMap<T, MappingRegistration<T>> = LinkedHashMap()

        /**
         * 存放[CorsConfiguration]的映射关系，提供根据Key([HandlerMethod])去寻找[CorsConfiguration]的方法；
         * [CorsConfiguration]内部维护的是一个CORS跨域的配置信息(可以从@CorsOrigin注解当中去进行探测)
         */
        private val corsLookup: MutableMap<HandlerMethod, CorsConfiguration> = LinkedHashMap()

        /**
         * 获取MappingRegistry的读锁
         */
        fun acquireReadLock() = this.readWriteLock.readLock().lock()

        /**
         * 释放MappingRegistry的读锁
         */
        fun releaseReadLock() = this.readWriteLock.readLock().unlock()

        /**
         * 获取注册中心当中全部已经注册的表项(Mapping->MappingRegistration)
         *
         * @return 获取当前的MappingRegistry当中的所有的所有注册的MappingRegistration列表
         */
        fun getRegistrations(): Map<T, MappingRegistration<T>> = this.registry

        /**
         * 根据直接路径去获取到注册的Mapping
         *
         * @return 根据directPath去获取到对应的Mapping列表
         */
        fun getMappingsByDirectPath(lookupPath: String): List<T> = pathLookup[lookupPath] ?: emptyList()

        /**
         * 根据给定的HandlerMethod，去找到合适的CorsConfiguration；
         * 因为HandlerMethod，很可能是将beanName解析成为了beanObject，
         * 因此，我们有可能需要获取的是原始的HandlerMethod
         *
         * @param handlerMethod HandlerMethod
         * @return 根据HandlerMethod去找到的合适的Cors配置信息
         */
        @Nullable
        fun getCorsConfiguration(handlerMethod: HandlerMethod): CorsConfiguration? {
            return this.corsLookup[handlerMethod.resolvedFromHandlerMethod ?: handlerMethod]
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
                // 如果有命名策略的话，那么需要生成mappingName并去注册
                val namingStrategy = getHandlerMethodMappingNamingStrategy()
                Optional.ofNullable(namingStrategy).ifPresent {
                    mappingName = it.getName(handlerMethod, mapping)
                    addNameToLookup(mappingName!!, handlerMethod)
                }

                // 交给子类去初始化CorsConfiguration，将该HandlerMethod的CorsConfiguration去进行注册
                val corsConfig = initCorsConfiguration(handler, method, mapping)
                if (corsConfig != null) {
                    this.corsLookup[handlerMethod] = corsConfig
                }

                // 将Mapping作为key，注册到registry当中
                this.registry[mapping] =
                    MappingRegistration(mapping, paths, handlerMethod, mappingName, corsConfig != null)
            } finally {
                this.readWriteLock.writeLock().unlock()  // releaseWriteLock
            }
        }

        /**
         * 将name->List<HandlerMethod>注册到MappingRegistry当中
         *
         * @param name name
         * @param handlerMethod HandlerMethod
         */
        private fun addNameToLookup(name: String, handlerMethod: HandlerMethod) {
            var handlerMethods = this.nameLookup[name]
            if (handlerMethods == null) {
                handlerMethods = ArrayList()
            }

            // 检查是否已经存在，如果直接存在，那么直接pass掉
            handlerMethods.forEach {
                if (it == handlerMethod) {
                    return
                }
            }
            // 如果之前并不存在的话，那么我们需要去copy一份，去进行添加，使用CopyOnWrite机制去进行实现
            val newHandlerMethods = ArrayList(handlerMethods)
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
            this.pathLookup.add(path, mapping)
        }
    }

    /**
     * Mapping的一个注册表项，内部封装了各种信息
     *
     * @param mapping mapping
     * @param directPaths 路径列表
     * @param handlerMethod handlerMethod(bean+method)
     * @param name mappingName
     * @param corsConfig 该Registration是否有CorsConfig？
     */
    data class MappingRegistration<T>(
        val mapping: T,
        val directPaths: Set<String>,
        val handlerMethod: HandlerMethod,
        val name: String?,
        val corsConfig: Boolean
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

    /**
     * 设置HandlerMethodMapping的命名生成策略
     *
     * @param namingStrategy 你想要使用的namingStrategy
     */
    open fun setHandlerMethodMappingNamingStrategy(@Nullable namingStrategy: HandlerMethodMappingNamingStrategy<T>?) {
        this.namingStrategy = namingStrategy
    }

    /**
     * 获取HandlerMethodMapping的命名生成策略
     *
     * @return namingStrategy
     */
    @Nullable
    open fun getHandlerMethodMappingNamingStrategy(): HandlerMethodMappingNamingStrategy<T>? = this.namingStrategy
}