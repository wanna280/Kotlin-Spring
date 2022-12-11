package com.wanna.boot.web.embedded.tomcat

import org.apache.catalina.Context

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
fun interface TomcatContextCustomizer {

    fun customize(context: Context)
}