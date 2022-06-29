package com.wanna.framework.web.method.view

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.aware.BeanClassLoaderAware
import com.wanna.framework.core.Ordered
import com.wanna.framework.web.handler.ViewResolver
import com.wanna.framework.web.ui.View

open class TemplateViewResolver : ViewResolver, BeanClassLoaderAware, Ordered, ApplicationContextAware {

    companion object {
        const val TEMPLATE_BASE_URL = "templates/"

        const val TEMPLATE_SUFFIX = ".html"
    }

    private var beanClassLoader: ClassLoader? = null

    private var applicationContext: ApplicationContext? = null

    private var order = Ordered.ORDER_LOWEST

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.beanClassLoader = classLoader
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    override fun getOrder(): Int {
        return order
    }

    override fun resolveView(viewName: String): View? {
        val classLoader = this.beanClassLoader ?: TemplateView::class.java.classLoader
        val resourcePath = TEMPLATE_BASE_URL + viewName + TEMPLATE_SUFFIX
        val fis = classLoader.getResourceAsStream(resourcePath)
        if (fis != null) {
            val templateView = TemplateView()
            templateView.viewName = viewName
            templateView.resourceStream = fis
            templateView.resourceUrl = resourcePath
            return templateView
        }
        return null
    }
}