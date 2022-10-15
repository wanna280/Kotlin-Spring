package com.wanna.boot.devtools.autoconfigure

/**
 * "RemoteServer"的"DevTools"的配置信息，用于需要去进行重启的服务端去进行配置
 *
 * @see DevToolsProperties
 */
class RemoteDevToolsProperties {
    companion object {
        const val DEFAULT_CONTEXT_PATH = "/spring-devtools"
        const val DEFAULT_SECRET_HEADER_NAME = "X-AUTH-TOKEN"
    }

    // RemoteServer的ContextPath
    var contextPath: String = DEFAULT_CONTEXT_PATH

    // 携带Secret的headerName
    var secretHeaderName: String = DEFAULT_SECRET_HEADER_NAME

    // 需要携带的Secret
    var secret: String = ""

    // 是否要开启Restart？
    var restart = Restart()

    class Restart {
        var enabled = true
    }
}