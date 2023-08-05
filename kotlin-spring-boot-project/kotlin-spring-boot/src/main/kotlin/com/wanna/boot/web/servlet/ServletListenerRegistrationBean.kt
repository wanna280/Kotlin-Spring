package com.wanna.boot.web.servlet

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import java.util.*
import javax.servlet.*
import javax.servlet.http.HttpSessionAttributeListener
import javax.servlet.http.HttpSessionListener
import kotlin.jvm.Throws

/**
 * ServletListener的RegistrationBean的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/12
 *
 * @see RegistrationBean
 */
open class ServletListenerRegistrationBean<T : EventListener>() : RegistrationBean() {

    companion object {
        /**
         * 支持去进行处理的EventListener的类型列表
         */
        @JvmStatic
        private val SUPPORTED_TYPES = setOf(
            ServletContextAttributeEvent::class.java,
            ServletContextListener::class.java,

            ServletRequestListener::class.java,
            ServletRequestAttributeListener::class.java,

            HttpSessionListener::class.java,
            HttpSessionAttributeListener::class.java,
        )

        /**
         * 检查给定的listener, 是否是一个合格的Servlet相关的EventListener
         *
         * @param listener 待检查的EventListener
         * @return 如果是Servlet相关的Listener, return true; 否则return false
         */
        @JvmStatic
        fun isSupportedType(listener: EventListener): Boolean {
            SUPPORTED_TYPES.forEach {
                if (ClassUtils.isAssignFrom(it, listener::class.java)) {
                    return true
                }
            }
            return false
        }

        /**
         * 获取所有的支持的EventListener类型
         *
         * @return supported event listener types
         */
        @JvmStatic
        fun getSupportedTypes(): Set<Class<*>> = this.SUPPORTED_TYPES
    }

    /**
     * Listener
     */
    private var listener: T? = null

    /**
     * 提供一个带参数的构造器
     *
     * @param listener listener
     * @throws IllegalStateException 如果给定的Listener, 不是支持去进行处理的Listener
     * @see SUPPORTED_TYPES
     */
    @Throws(IllegalStateException::class)
    constructor(listener: T) : this() {
        if (isSupportedType(listener)) {
            throw IllegalStateException("Given listener is not a supported type")
        }
        this.listener = listener
    }

    /**
     * set Listener
     *
     * @param listener listener
     * @throws IllegalStateException 如果给定的Listener, 不是支持去进行处理的Listener
     * @see SUPPORTED_TYPES
     */
    @Throws(IllegalStateException::class)
    open fun setListener(listener: T) {
        if (isSupportedType(listener)) {
            throw IllegalStateException("Given listener is not a supported type")
        }
        this.listener = listener
    }

    /**
     * get Listener
     */
    @Nullable
    open fun getListener(): T? = this.listener

    /**
     * 获取当前的ServletListener的RegistrationBean的描述信息
     *
     * @return description
     */
    override fun getDescription(): String = "listener $listener"

    /**
     * 真正地去将Listener去注册到ServletContext当中去
     *
     * @param description description
     * @param servletContext ServletContext
     *
     * @throws IllegalStateException 如果将Listener添加到ServletContext当中失败
     */
    @Throws(IllegalStateException::class)
    override fun register(description: String, servletContext: ServletContext) {
        try {
            servletContext.addListener(listener)
        } catch (ex: Exception) {
            throw IllegalStateException("Failed to add listener $listener to ServletContext", ex)
        }
    }
}