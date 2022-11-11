package com.wanna.framework.context.event

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.Order
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import com.wanna.framework.util.StringUtils
import java.lang.reflect.Method

/**
 * 将一个标注了[EventListener]注解的方法, 去转换成为一个[ApplicationListener]的适配器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/7
 *
 * @param beanName beanName
 * @param targetMethod targetMethod
 * @param targetClass targetClass
 */
open class ApplicationListenerMethodAdapter(
    private val beanName: String,
    private val targetClass: Class<*>,
    private val targetMethod: Method
) : GenericApplicationListener {

    companion object {

        /**
         * 从给定的方法上去解析到它需要去进行处理的事件类型
         *
         * @param method method
         * @param ann 该方法上的@EventListener注解(可以没有, 此时我们选取方法的第一个参数作为eventType)
         * @return 从给定的方法上去解析到的事件类型列表
         * @throws IllegalStateException 如果给定的方法的参数不为一个(多余1个, 或者没有参数都算)
         */
        @JvmStatic
        @Throws(IllegalStateException::class)
        private fun resolveDeclaredEventTypes(method: Method, @Nullable ann: EventListener?): List<ResolvableType> {
            val count = method.parameterCount
            if (count > 1) {
                throw IllegalStateException("EventListener方法[$method]最多只能有一个方法参数")
            }

            // 如果找到了@EventListener注解
            if (ann != null) {
                val eventClasses = ann.classes + ann.value
                if (eventClasses.isNotEmpty()) {
                    return eventClasses.map { it.java }.map { ResolvableType.forClass(it) }
                }
            }
            if (count == 0) {
                throw IllegalStateException("EventListener方法[$method]必须要有Event参数")
            }
            // EventListener只有一个方法参数, 那么就选取它去作为Event
            return listOf(ResolvableType.forMethodParameter(MethodParameter(method, 0)))
        }

        /**
         * 从给定的方法上去解析出来Order优先级
         *
         * @param method 待解析Order的方法
         * @return 解析得到的Order优先级(如果有@Order注解的话, 返回order; 如果没有@Order注解的话, 返回lowest)
         */
        @JvmStatic
        private fun resolveOrder(method: Method): Int {
            val order = AnnotatedElementUtils.getMergedAnnotation(method, Order::class.java)
            return order?.value ?: Ordered.ORDER_LOWEST
        }
    }

    /**
     * 当前ApplicationListener(EventListener)支持去进行处理的事件列表
     */
    private var declaredEventTypes: List<ResolvableType>

    /**
     * ApplicationContext
     */
    @Nullable
    private var applicationContext: ApplicationContext? = null

    /**
     * Order
     */
    private var order = Ordered.ORDER_LOWEST

    /**
     * ListenerId
     */
    @Nullable
    private var listenerId: String?

    init {
        // 解析到目标方法上的@EventListener注解
        val ann = AnnotatedElementUtils.getMergedAnnotation(targetMethod, EventListener::class.java)

        // 根据@EventListener去解析支持去进行处理的事件类型
        this.declaredEventTypes = resolveDeclaredEventTypes(targetMethod, ann)

        // 解析Order
        this.order = resolveOrder(targetMethod)

        // 解析ListenerId
        this.listenerId = if (StringUtils.hasText(ann?.id)) ann?.id else null
    }


    /**
     * 当事件发布时, 我们应该怎么去进行处理?
     *
     * @param event event
     */
    override fun onApplicationEvent(event: ApplicationEvent) {
        processEvent(event)
    }

    /**
     * 处理给定的一个[ApplicationEvent]事件
     *
     * @param event event
     */
    open fun processEvent(event: ApplicationEvent) {
        // 解析得到执行目标@EventListener方法需要用到的参数
        // 如果无法解析到合适的方法参数的话, 直接return null
        val arguments = resolveArguments(event) ?: return

        // 执行目标@EventListener方法
        doInvoke(arguments)
    }

    /**
     * 执行目标@EventListener方法
     *
     * @param args 方法参数列表
     */
    protected open fun doInvoke(args: Array<Any?>) {
        val bean = getTargetBean()
        // 如果是一个NullBean, 那么直接pass掉
        if (bean.equals(null)) {
            return
        }

        // 反射执行目标方法
        ReflectionUtils.makeAccessible(targetMethod)
        ReflectionUtils.invokeMethod(targetMethod, bean, *args)
    }

    /**
     * 获取到执行@EventListener方法需要用到的Bean
     *
     * @return bean
     */
    protected open fun getTargetBean(): Any {
        return this.applicationContext?.getBean(beanName) ?: throw IllegalStateException("ApplicationContext不能为null")
    }

    /**
     * 根据[event]解析得到合适的方法参数列表
     *
     * @param event 当前正在发布的事件event
     * @return 解析得到的执行目标方法需要用到的参数(如果return null, 代表当前Listener不支持去处理这样的事件类型, 需要跳过)
     */
    protected open fun resolveArguments(event: ApplicationEvent): Array<Any?>? {
        // 如果event和所有的定义的事件进行匹配, 最终类型都不匹配的话, 直接return null; 代表了当前事件其实不应该当前Listener触发
        // 其实很多判断其实应该在supports方法当中去进行判断的, 但是payload的泛型无法解析到, 只能被迫放在这
        val declaredEventType = getResolvableType(event) ?: return null

        // 如果要去进行执行的该目标方法没有参数的话, 那么直接return emptyArray
        if (targetMethod.parameterCount == 0) {
            return emptyArray()
        }

        // 如果event匹配到了一个declaredEventType的话, 那么我们计算一下是否匹配?
        val declaredEventClass = declaredEventType.resolve()!!

        // 如果定义的事件类型不是ApplicationEvent的话, 那么我们还需要匹配一下payload和declaredEventType的类型
        if (!ClassUtils.isAssignFrom(
                ApplicationEvent::class.java,
                declaredEventClass
            ) && event is PayloadApplicationEvent<*>
        ) {
            val payload = event.payload
            if (ClassUtils.isAssignFrom(declaredEventClass, payload::class.java)) {
                return arrayOf(payload)
            }
        }

        // 如果定义的事件就是ApplicationEvent的话, 那么直接返回ApplicationEvent
        return arrayOf(event)
    }

    /**
     * 根据给定的event去匹配, 解析出来在当前的[ApplicationListener]当中已经被定义的类型;
     * 和所有的已经定义的事件类型去进行匹配, 如果存在有其中一个匹配的, 那么直接返回该eventType(返回定义的).
     *
     * @param event event
     * @return 解析出来对应的定义的eventType事件类型, 如果无法解析到return null
     */
    @Nullable
    private fun getResolvableType(event: ApplicationEvent): ResolvableType? {
        var payloadType: ResolvableType? = null
        // 如果需要去进行发布的是PayloadApplicationEvent事件的话, 那么我们需要去获取到对应的payload类型
        if (event is PayloadApplicationEvent<*>) {

            // 这里我们从event当中去获取到ResolvableType(因为只有它自己才能解析到自己的真正泛型)
            val type = event.getResolvableType()
            payloadType = type.`as`(PayloadApplicationEvent::class.java).getGenerics()[0]
        }

        // 根据当前@EventListener方法当中已经定义的事件类型, 和payloadType去进行匹配
        this.declaredEventTypes.forEach {

            // 看看@EventListener当中定义的事件类型是什么?
            val declaredEventClass = it.resolve()!!


            // 如果定义的事件类型不是ApplicationEvent, 那么就是一个很普通的Java对象, 那么直接和payload类型去进行匹配即可
            if (!ClassUtils.isAssignFrom(ApplicationEvent::class.java, declaredEventClass)
                && payloadType != null && ClassUtils.isAssignFrom(declaredEventClass, payloadType.resolve()!!)
            ) {
                return it
            }

            // 如果定义的就是ApplicationEvent, 那么检查给的event是否是定义的declaredEventClass的子类?
            if (ClassUtils.isAssignFrom(declaredEventClass, event::class.java)) {
                return it
            }
        }
        return null
    }

    /**
     * 当前[ApplicationListener]是否支持去处理这样的事件类型
     *
     * @param type eventType
     * @return 如果当前[ApplicationListener]支持去处理该事件, return true; 否则return false
     */
    override fun supportsEventType(type: ResolvableType): Boolean {
        this.declaredEventTypes.forEach { declaredEventType ->

            // 检查是否支持去进行处理?EventType是否直接匹配
            if (ClassUtils.isAssignFrom(declaredEventType.resolve(), type.resolve())) {
                return true
            }

            // 如果你给定的是一个PayloadApplicationEvent(那么还需要去检查一下Payload是否和当前事件类型匹配?)
            if (ClassUtils.isAssignFrom(PayloadApplicationEvent::class.java, type.resolve())) {

                // 解析得到PayloadApplicationEvent的泛型(正常情况都无法解析出来)
                val payloadEventType = type.`as`(PayloadApplicationEvent::class.java).getGenerics()[0]
                val payloadType = payloadEventType.resolve()

                // 1.如果无法解析出来PayloadApplicationEvent泛型, 那么算是匹配, 在发布事件时再去匹配(目前无法做到匹配了)
                // 2.如果解析到了payload的类型的话, 那么检查一下payload和@EventListener方法上定义的事件类型是否匹配?
                if (payloadType == null || ClassUtils.isAssignFrom(declaredEventType.resolve(), payloadType)) {
                    return true
                }
            }
        }
        return false
    }

    override fun getOrder(): Int = this.order

    /**
     * 完成[ApplicationListener]的初始化
     *
     * @param context ApplicationContext
     */
    open fun init(context: ApplicationContext) {
        this.applicationContext = context
    }
}