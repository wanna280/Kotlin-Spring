package com.wanna.boot.devtools

import com.wanna.boot.context.event.ApplicationEnvironmentPreparedEvent
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.core.environment.CommandLinePropertySource
import com.wanna.framework.core.environment.MapPropertySource
import com.wanna.framework.util.StringUtils
import java.net.URI
import java.net.URISyntaxException

/**
 * "remoteUrl"的属性提取器, 将命令行当中以nonOptionArgs的方式传递的"remoteUrl",
 * 包装成为一个PropertySource放入到Environment当中, 方便直接使用@Value注解去进行注入
 *
 * @see RemoteSpringApplication
 * @see com.wanna.boot.devtools.remote.client.RemoteClientConfiguration
 */
open class RemoteUrlPropertyExtractor : ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    companion object {
        private const val NON_OPTION_ARGS = CommandLinePropertySource.DEFAULT_NO_OPTION_ARGS_PROPERTY_NAME
    }

    override fun onApplicationEvent(event: ApplicationEnvironmentPreparedEvent) {
        val environment = event.environment
        // 获取NonOptionArgs作为Url
        val remoteUrl = getCleanRemoteUrl(environment.getProperty(NON_OPTION_ARGS))
        if (!StringUtils.hasText(remoteUrl)) {
            throw IllegalStateException("没有指定remoteUrl, 请在命令行当中指定remoteUrl")
        }

        // 验证给定的"remoteUrl"是否合法, 如果不合法需要抛出异常
        try {
            URI(remoteUrl!!)
        } catch (ex: URISyntaxException) {
            throw IllegalStateException("通过命令行传递的remoteUrl不合法[$remoteUrl]")
        }

        // add PropertySource
        environment.getPropertySources().addLast(MapPropertySource("remoteUrl", mapOf("remoteUrl" to remoteUrl)))
    }

    /**
     * 获取干净的remoteUrl
     *
     * @param url 原始的url
     * @return 干净的url
     */
    private fun getCleanRemoteUrl(url: String?): String? =
        if (StringUtils.hasText(url) && url!!.endsWith("/")) url.substring(0, url.length - 1) else url
}