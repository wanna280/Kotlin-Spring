package com.wanna.boot.context.config

import com.wanna.framework.core.environment.PropertySource
import java.util.EventListener

/**
 * 对于ConfigData的Environment发生变更时的Listener
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 */
interface ConfigDataEnvironmentUpdateListener : EventListener {

    companion object {

        /**
         * None的单例对象
         */
        @JvmStatic
        val NONE = object : ConfigDataEnvironmentUpdateListener {}
    }

    /**
     * 当一个PropertySource被添加
     *
     * @param propertySource PropertySource
     * @param location ConfigDataLocation
     * @param resource resource
     */
    fun onPropertySourceAdded(
        propertySource: PropertySource<*>,
        location: ConfigDataLocation,
        resource: ConfigDataResource
    ) {
    }

    /**
     * 在profiles被设置的时候, 被自动Callback
     *
     * @param profiles Profiles
     */
    fun onSetProfiles(profiles: Profiles) {}

}