package com.wanna.framework.context.event

import java.util.EventObject

/**
 * 这是一个要进行发布的事件对象
 */
open class ApplicationEvent(source: Any?) : EventObject(source) {

}