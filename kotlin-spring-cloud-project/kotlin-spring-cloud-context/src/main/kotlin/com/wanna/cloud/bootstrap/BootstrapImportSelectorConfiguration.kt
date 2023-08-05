package com.wanna.cloud.bootstrap

import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Import
import com.wanna.framework.context.annotation.ImportSelector

/**
 * 这是一个给容器中导入[BootstrapImportSelector]的一个配置类, 因为[ImportSelector]只能被配置类使用`@Import`等方式去进行所导入才能生效...
 *
 * @see BootstrapImportSelector
 */
@Configuration(proxyBeanMethods = false)
@Import([BootstrapImportSelector::class])
class BootstrapImportSelectorConfiguration