package com.wanna.cloud.bootstrap

import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Import

/**
 * 这是一个给容器中导入BootstrapImportSelector的一个配置类，因为ImportSelector只能被配置类所导入才能生效...
 */
@Configuration(proxyBeanMethods = false)
@Import([BootstrapImportSelector::class])
class BootstrapImportSelectorConfiguration