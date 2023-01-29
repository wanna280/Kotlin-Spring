package com.wanna.framework.context

import com.wanna.framework.beans.factory.Aware
import com.wanna.framework.context.event.ApplicationEventPublisher

/**
 * 这是一个ApplicationEvent的发布器的Aware接口, 通过它可以给Bean设置ApplicationEventPublisher完成事件的发布
 */
interface ApplicationEventPublisherAware : Aware {
    fun setApplicationEventPublisher(publisher: ApplicationEventPublisher)
}