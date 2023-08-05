package com.wanna.boot.devtools.remote.client

import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.boot.devtools.autoconfigure.DevToolsProperties
import com.wanna.boot.devtools.autoconfigure.TriggerFileFilter
import com.wanna.boot.devtools.classpath.ClassPathFileSystemWatcher
import com.wanna.boot.devtools.classpath.ClassPathRestartStrategy
import com.wanna.boot.devtools.classpath.PatternClassPathRestartStrategy
import com.wanna.boot.devtools.filewatch.FileSystemWatcher
import com.wanna.boot.devtools.filewatch.FileSystemWatcherFactory
import com.wanna.boot.devtools.filewatch.SnapshotStateRepository
import com.wanna.boot.devtools.restart.DefaultRestartInitializer
import com.wanna.boot.devtools.restart.classloader.ClassLoaderFiles
import com.wanna.framework.beans.factory.annotation.Value
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.util.StringUtils
import com.wanna.framework.web.http.client.ClientHttpRequestFactory
import com.wanna.framework.web.http.client.SimpleClientHttpRequestFactory

/**
 * "DevTools"的RemoteClient的配置类, 提供监控本地文件, 当本地文件发生变更时,
 * 自动将变更情况推送给RemoteServer, RemoteServer则会接收此变更信息, 并使用RestartClassLoader
 * 去加载变更的文件信息, 并去进行重启RemoteServer的SpringApplication
 */
@EnableConfigurationProperties([DevToolsProperties::class])
@Configuration(proxyBeanMethods = false)
open class RemoteClientConfiguration {

    /**
     * ClientHttpRequestFactory, 提供HTTP请求的发送的客户端功能
     *
     * @return ClientHttpRequestFactory
     */
    @Bean
    @ConditionalOnMissingBean
    open fun remoteClientHttpRequestFactory(): ClientHttpRequestFactory {
        return SimpleClientHttpRequestFactory()
    }

    /**
     * 远程重启的客户端的配置类, 负责在本地的源码发生变更时, 直接将更改信息(包装到ClassLoaderFiles)上传给RemoteServer
     * RemoteServer端, 需要接收客户端发送过去的ClassLoaderFiles, 并设置到Restarter.classLoaderFiles当中,
     * 从而下次重启时, RestartClassLoader在进行类加载时, 就会优先从给定的ClassLoaderFiles当中去检查是否有更新的文件,
     * 如果有优先的ClassLoaderFile, 则使用优先的ClassLoaderFile作为真正地去加载的类, 而不是去本地检查, 从而实现远程的
     * SpringApplication的热部署功能
     */
    @Configuration(proxyBeanMethods = false)
    open class RemoteRestartClientConfiguration {
        /**
         * 自动注入DevTools的配置信息
         */
        @Autowired
        private lateinit var properties: DevToolsProperties

        /**
         * ClassPath的变更的Uploader, 负责将本地的文件变更情况包装成为[ClassLoaderFiles]直接上传给RemoteServer
         *
         * @param remoteUrl remoteUrl(需要去进行指定)
         * @param clientHttpRequestFactory 发生HTTP请求Client的Factory
         */
        @Bean
        open fun classPathChangeUploader(
            @Value("${'$'}{remoteUrl}") remoteUrl: String,
            clientHttpRequestFactory: ClientHttpRequestFactory
        ): ClassPathChangeUploader {
            val url = remoteUrl + properties.remote.contextPath + "/restart"
            return ClassPathChangeUploader(url, clientHttpRequestFactory)
        }

        /**
         * 给Spring BeanFactory当中去添加一个ClassPath下的文件系统的Watcher
         *
         * @param factory 提供FileSystemWatcher的Factory
         * @param pathRestartStrategy Restart策略(只有在改变的文件符合该策略的规则时才去进行重启)
         * @return ClassPathFileSystemWatcher
         */
        @Bean
        open fun classFilePathSystemWatcher(
            factory: FileSystemWatcherFactory, pathRestartStrategy: ClassPathRestartStrategy
        ): ClassPathFileSystemWatcher {
            // 获取到需要去重新加载的目录的URL列表
            val urls = DefaultRestartInitializer().getInitialUrls(Thread.currentThread()) ?: emptyArray()

            // 创建一个ClassPath下的文件的Watcher, 去监听本地文件的变化
            val classPathFileSystemWatcher =
                ClassPathFileSystemWatcher(factory.getFileSystemWatcher(), urls, pathRestartStrategy)


            // 对于RemoteClient来说, 当发布ClassPathChangedEvent来说, 别去重启Watcher!!!
            // 因为对于RemoteClient来说, 它不会接收ClassPathChangedEvent而重启,
            // Watcher需要继续工作, 当客户端文件发生变更时, 继续将文件上传给RemoteServer
            classPathFileSystemWatcher.stopWatcherOnRestart = false
            return classPathFileSystemWatcher
        }

        /**
         * FileSystemWatcher的Factory, 负责提供FileSystemWatcher的创建
         *
         * @see FileSystemWatcher
         * @see FileSystemWatcherFactory
         */
        @Bean
        @ConditionalOnMissingBean
        open fun fileSystemWatcherFactory(): FileSystemWatcherFactory {
            return FileSystemWatcherFactory { // 创建FileSystemWatcher
                val fileSystemWatcher =
                    FileSystemWatcher(
                        true, properties.restart.pollInterval, properties.restart.quietPeriod,
                        SnapshotStateRepository.STATIC
                    )
                // 如果配置文件当中, 指定了触发的Restart文件的话, 需要添加TriggerFileFilter
                val triggerFile = properties.restart.triggerFile
                if (StringUtils.hasText(triggerFile)) {
                    fileSystemWatcher.setTriggerFilter(TriggerFileFilter(triggerFile!!))
                }
                fileSystemWatcher
            }
        }

        /**
         * 给Spring BeanFactory当中去导入一个基于路径匹配的重启策略的Bean
         *
         * @return PatternClassPathRestartStrategy
         */
        @Bean
        @ConditionalOnMissingBean
        open fun patternClassPathRestartStrategy(): PatternClassPathRestartStrategy {
            return PatternClassPathRestartStrategy(properties.restart.exclude)
        }
    }
}