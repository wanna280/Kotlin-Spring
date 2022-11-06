package com.wanna.framework.test.context.event

/**
 * 使用[ThreadLocal]的方式去维护[ApplicationEvents]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/6
 */
object ApplicationEventsHolder {

    /**
     * 使用ThreadLocal维护[ApplicationEvents]
     */
    @JvmStatic
    private val applicationEvents = ThreadLocal<DefaultApplicationEvents>()

    /**
     * 从当前线程的[ApplicationEventsHolder]当中去获取[ApplicationEvents]
     *
     * @return ApplicationEvents(如果不存在的话，return null)
     */
    @JvmStatic
    fun getApplicationEvents(): ApplicationEvents? {
        return applicationEvents.get()
    }

    /**
     * 从当前线程的[ApplicationEventsHolder]当中去获取[ApplicationEvents]，如果不存在的话，丢出[IllegalStateException]异常
     *
     * @return ApplicationEvents(never null)
     * @throws IllegalStateException 如果ApplicationEvents为null
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    fun getRequiredApplicationEvents(): ApplicationEvents {
        return applicationEvents.get()
            ?: throw IllegalStateException("无法根据当前线程去获取到ApplicationEvents, 请确保TestClass上标注了@RecordApplicationEvents注解")
    }

    /**
     * 如果必要的话(如果之前还不存在有[ApplicationEvents]的话)，
     * 往当前线程的[ApplicationEventsHolder]当中去注册一个[ApplicationEvents]
     */
    @JvmStatic
    fun registerApplicationEventsIfNecessary() {
        if (getApplicationEvents() == null) {
            registerApplicationEvents()
        }
    }

    /**
     * 往当前线程的[ApplicationEventsHolder]当中去维护一个[ApplicationEvents]
     */
    @JvmStatic
    fun registerApplicationEvents() {
        applicationEvents.set(DefaultApplicationEvents())
    }

    /**
     * 从当前线程的[ApplicationEventsHolder]当中去移除掉已经有的[ApplicationEvents]
     */
    @JvmStatic
    fun unregisterApplicationEvents() {
        applicationEvents.remove()
    }
}