package com.wanna.framework.web.ui

/**
 * ModelMap，存放模型数据
 */
class ModelMap : LinkedHashMap<String, Any>(), Model {
    override fun asMap(): Map<String, Any> {
        return this
    }
}