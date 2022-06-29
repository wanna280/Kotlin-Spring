package com.wanna.framework.web.config.annotation

import com.wanna.framework.web.accept.ContentNegotiationManager

open class ContentNegotiationConfigurer {

    fun build(): ContentNegotiationManager{
        return ContentNegotiationManager()
    }
}