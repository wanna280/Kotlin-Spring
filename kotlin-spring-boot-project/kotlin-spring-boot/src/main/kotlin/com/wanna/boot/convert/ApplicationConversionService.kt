package com.wanna.boot.convert

import com.wanna.framework.core.convert.converter.ConverterRegistry
import com.wanna.framework.core.convert.support.DefaultConversionService
import com.wanna.framework.core.convert.support.GenericConversionService

/**
 * Application ConversionService
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/5
 */
open class ApplicationConversionService : GenericConversionService() {

    init {
        configure(this)
    }

    companion object {
        /**
         * 共享的[ApplicationConversionService]实例
         */
        @Volatile
        @JvmStatic
        private var sharedInstance: ApplicationConversionService? = null

        /**
         * 获取共享的ConversionService，使用DCL完成获取
         *
         * @return 共享的DefaultConversionService
         */
        @JvmStatic
        fun getSharedInstance(): ApplicationConversionService {
            var sharedInstance = this.sharedInstance
            if (sharedInstance == null) {
                synchronized(ApplicationConversionService::class.java) {
                    sharedInstance = this.sharedInstance
                    if (sharedInstance == null) {
                        sharedInstance = ApplicationConversionService()
                    }
                }
            }
            return sharedInstance!!
        }

        /**
         * Configure
         *
         * @param registry ConverterRegistry
         */
        @JvmStatic
        fun configure(registry: ConverterRegistry) {
            DefaultConversionService.addDefaultConverters(registry)
        }
    }

}