package com.wanna.framework.web.server.servlet

import com.wanna.framework.context.aware.EnvironmentAware
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.EnvironmentCapable
import com.wanna.framework.core.environment.StandardEnvironment
import com.wanna.framework.lang.Nullable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServlet

/**
 * 简单的[HttpServlet]的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/11
 */
abstract class HttpServletBean : HttpServlet(), EnvironmentCapable, EnvironmentAware {

    /**
     * Logger
     */
    protected val logger: Logger = LoggerFactory.getLogger(HttpServletBean::class.java)


    /**
     * Environment
     */
    @Nullable
    private var environment: ConfigurableEnvironment? = null

    /**
     * 获取Environment
     *
     * @return Environment, 如果之前还不存在这样的Environment的话, 那么先创建再去进行返回
     */
    override fun getEnvironment(): Environment {
        if (this.environment == null) {
            this.environment = createEnvironment()
        }
        return this.environment!!
    }

    /**
     * 设置[Environment]
     *
     * @param environment Environment
     */
    override fun setEnvironment(environment: Environment) {
        if (environment is ConfigurableEnvironment) {
            this.environment = environment
        }
    }

    protected open fun createEnvironment(): ConfigurableEnvironment {
        return StandardEnvironment()
    }
}