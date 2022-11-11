package com.wanna.boot.autoconfigure

/**
 * 这是一个自动配置的ImportFilter，协助去完成条件装配，实现完成大多数的容器当中不需要的Bean的过滤；
 * 这个Filter一般需要结合一些Aware接口去注入相关的容器对象去进行使用，方便去完成匹配
 */
interface AutoConfigurationImportFilter {

    /**
     * @param autoConfigurationClasses SpringFactories当中的要进行AutoConfiguration的className列表(某个元素可能为null，代表之前的Filter已经将其过滤掉了)
     * @param autoConfigurationMetadata "META-INF/spring-autoconfigure-metadata.properties"当中加载的一些元信息
     * @return matches结果，matches(index)==false意味着该位置的配置类将会被过滤掉
     */
    fun matches(
        autoConfigurationClasses: Array<String?>,
        autoConfigurationMetadata: AutoConfigurationMetadata
    ): Array<Boolean>
}