package com.wanna.framework.test

import com.wanna.framework.context.annotations.ComponentScan
import com.wanna.framework.context.annotations.ImportSource
import com.wanna.framework.context.annotations.XmlBeanDefinitionReader

@ImportSource(reader = XmlBeanDefinitionReader::class)
@ComponentScan(["com.wanna"])
class AppConfiguration