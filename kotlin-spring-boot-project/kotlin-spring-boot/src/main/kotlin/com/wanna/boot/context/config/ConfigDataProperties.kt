package com.wanna.boot.context.config

import com.wanna.boot.cloud.CloudPlatform
import com.wanna.boot.context.properties.bind.Bindable
import com.wanna.boot.context.properties.bind.Binder
import com.wanna.boot.context.properties.bind.Name
import com.wanna.boot.context.properties.source.ConfigurationPropertyName
import com.wanna.framework.lang.Nullable

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 *
 * @param imports 需要进行配置文件的处理的位置的ConfigDataLocation
 * @param activate 需要去进行激活使用的Profile
 */
class ConfigDataProperties(
    @Name("import") val imports: List<ConfigDataLocation>,
    @Nullable val activate: Activate? = null
) {

    companion object {
        /**
         * 要去进行绑定的配置属性前缀
         */
        @JvmStatic
        private val NAME = ConfigurationPropertyName.of("spring.config")

        /**
         * 对ConfigDataProperties对象去进行绑定的Bindable
         */
        @JvmStatic
        private val BINDABLE_PROPERTIES = Bindable.of(ConfigDataProperties::class.java)

        /**
         * 将"spring.config"的前缀绑定到到ConfigDataProperties当中来, 获取到ConfigDataProperties实例
         *
         * * 1.将"spring.config.import"前缀的配置绑定到imports字段当中
         * * 2.将"spring.config.activate"前缀的配置绑定到activate字段当中
         *
         * @param binder Binder
         * @return 执行绑定的结果(or null)
         */
        @Nullable
        @JvmStatic
        fun get(binder: Binder): ConfigDataProperties? {
            return binder.bind(NAME, BINDABLE_PROPERTIES).orElse(null)
        }
    }


    /**
     * 通过context去检查该Profiles是否被激活?
     *
     * @param activationContext ActivationContext
     * @return 如果该Profiles需要被激活, 那么return true; 否则return false
     */
    fun isActive(@Nullable activationContext: ConfigDataActivationContext?): Boolean {
        return activate == null || activate.isActive(activationContext)
    }


    class Activate {
        var onCloudPlatform: CloudPlatform? = null
        var onProfile: Array<String>? = null

        fun isActive(@Nullable activationContext: ConfigDataActivationContext?): Boolean {
            return false
        }
    }
}