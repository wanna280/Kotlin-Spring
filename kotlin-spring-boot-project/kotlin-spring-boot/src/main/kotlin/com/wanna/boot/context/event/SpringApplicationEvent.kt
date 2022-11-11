package com.wanna.boot.context.event

import com.wanna.boot.SpringApplication
import com.wanna.framework.context.event.ApplicationEvent

/**
 * 这是一个SpringApplicationEvent，它的source为SpringApplication，因此通过它可以获取到SpringApplication对象
 */
open class SpringApplicationEvent(val application: SpringApplication, val args: Array<String>) :
    ApplicationEvent(application)