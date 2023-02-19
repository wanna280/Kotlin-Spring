package com.wanna.debugger.bistoury.instrument.client.monitor

import com.wanna.debugger.bistoury.instrument.client.location.ResolvedSourceLocation

/**
 * 全局的MonitorContext, 维护已经添加监控的代码位置信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/31
 */
object BistouryGlobalMonitorContext {
    /**
     * Key的分隔符
     */
    private const val SEPARATOR = "|"

    /**
     * 已经添加监控的代码位置列表, Key格式为"{classSignature}|{methodName}|{methodDescriptor}"
     */
    @JvmStatic
    private val monitors = LinkedHashMap<String, Boolean>()

    /**
     * 添加一个已经添加动态监控的代码位置
     *
     * @param location 要去添加动态监控的代码位置
     */
    @JvmStatic
    fun addMonitor(location: ResolvedSourceLocation) {
        synchronized(monitors) {
            monitors[getLocationKey(location)] = true
        }
    }

    /**
     * 检查给定的代码位置, 是否已经添加过动态监控?
     *
     * @param location 要去进行检查的代码位置
     * @return 如果之前已经添加过动态监控, return true; 否则return false
     */
    @JvmStatic
    fun check(location: ResolvedSourceLocation): Boolean {
        synchronized(monitors) {
            return monitors.containsKey(getLocationKey(location))
        }
    }

    @JvmStatic
    private fun getLocationKey(location: ResolvedSourceLocation): String {
        return location.classSignature + SEPARATOR + location.methodName + SEPARATOR + location.methodDescriptor
    }

    /**
     * destroy, 销毁当前MonitorContext时, 需要清除缓存
     */
    @JvmStatic
    fun destroy() {
        this.monitors.clear()
    }
}