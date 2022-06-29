package com.wanna.boot.env

import com.wanna.framework.core.environment.MapPropertySource

open class OriginTrackedMapPropertySource(name: String, source: Map<String, Any>) : MapPropertySource(name, source) {

}