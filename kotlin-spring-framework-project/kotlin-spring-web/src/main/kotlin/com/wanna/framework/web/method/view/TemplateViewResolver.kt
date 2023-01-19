package com.wanna.framework.web.method.view

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.ResourceLoaderAware
import com.wanna.framework.context.aware.BeanClassLoaderAware
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.handler.ViewResolver
import com.wanna.framework.web.ui.View
import com.wanna.common.logging.LoggerFactory
import java.io.IOException

open class TemplateViewResolver : ViewResolver, BeanClassLoaderAware, Ordered, ApplicationContextAware,
    ResourceLoaderAware {

    companion object {

        /**
         * Logger
         */
        private val logger = LoggerFactory.getLogger(TemplateView::class.java)

        const val TEMPLATE_BASE_URL = "templates/"

        const val TEMPLATE_SUFFIX = ".html"
    }

    /**
     * ClassLoader
     */
    private var beanClassLoader: ClassLoader? = null

    /**
     * ApplicationContext
     */
    private var applicationContext: ApplicationContext? = null

    /**
     * ResourceLoader
     */
    private var resourceLoader: ResourceLoader? = null

    private var order = Ordered.ORDER_LOWEST

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.beanClassLoader = classLoader
    }

    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourceLoader = resourceLoader
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    override fun getOrder(): Int = this.order

    /**
     * 根据viewName去翻译得到对应的视图View对象
     *
     * @param viewName viewName
     * @return View
     */
    @Nullable
    override fun resolveViewName(viewName: String): View? {
        val resourcePath = TEMPLATE_BASE_URL + viewName + TEMPLATE_SUFFIX
        try {
            // 使用ResourceLoader去加载到对应的资源
            val fis = resourceLoader?.getResource(resourcePath)?.getInputStream()
            if (fis != null) {
                val templateView = TemplateView()
                templateView.viewName = viewName
                templateView.resourceStream = fis
                templateView.resourceUrl = resourcePath
                return templateView
            }
        } catch (ex: IOException) {
            logger.error("无法解析到资源文件[$resourcePath]", ex)
        }
        return null
    }
}