package com.wanna.boot.web.embedded.tomcat

import org.apache.catalina.core.StandardContext

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
open class TomcatEmbeddedContext : StandardContext() {

    /**
     * Tomcat Starter
     */
    private var tomcatStarter: TomcatStarter? = null


    /**
     * 初始化TomcatStarter
     *
     * @param tomcatStarter TomcatStarter
     */
    open fun setTomcatStarter(tomcatStarter: TomcatStarter) {
        this.tomcatStarter = tomcatStarter
    }

}