package com.wanna.cloud.bootstrap

/**
 * 用来给SpringCloud当中会用到的Bootstrap容器当中导入配置类, 在SpringFactories当中通过"BootstrapConfiguration"配置的配置类,
 * 将会被SpringCloudContext自动导入到SpringCloud的Bootstrap容器当中, 而不是被导入到用户真正使用的Root SpringApplication当中, 具体见[BootstrapImportSelector]
 *
 * @see BootstrapImportSelectorConfiguration
 * @see BootstrapImportSelector
 */
annotation class BootstrapConfiguration