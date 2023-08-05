package com.wanna.debugger.bistoury.instrument.agent

import java.net.URL
import java.net.URLClassLoader

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 */
open class BistouryAgentClassLoader(urls: Array<URL>, parent: ClassLoader) : URLClassLoader(urls, parent) {

}