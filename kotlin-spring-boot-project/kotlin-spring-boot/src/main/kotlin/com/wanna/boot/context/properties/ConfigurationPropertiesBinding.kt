package com.wanna.boot.context.properties

import com.wanna.framework.beans.factory.annotation.Qualifier

/**
 * 标注`@ConfigurationPropertiesBinding`注解的Bean(Converter/GenericConverter), 可以去对[ConfigurationProperties]的Bean去进行自定义的配置
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/9
 */
@Qualifier(ConfigurationPropertiesBinding.VALUE)
annotation class ConfigurationPropertiesBinding {
    companion object {
        const val VALUE = "com.wanna.boot.context.properties.ConfigurationPropertiesBinding"
    }
}
