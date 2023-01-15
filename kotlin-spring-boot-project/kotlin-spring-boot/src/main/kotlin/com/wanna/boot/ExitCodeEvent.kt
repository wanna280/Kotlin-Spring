package com.wanna.boot

import com.wanna.framework.context.event.ApplicationEvent

/**
 * 产生了ExitCode的事件, 当ExitCodeGenerator生成了ExitCode的话, 就会触发这个事件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/2
 * @see ExitCodeGenerator.getExitCode
 */
class ExitCodeEvent(source: Any, val exitCode: Int) : ApplicationEvent(source)