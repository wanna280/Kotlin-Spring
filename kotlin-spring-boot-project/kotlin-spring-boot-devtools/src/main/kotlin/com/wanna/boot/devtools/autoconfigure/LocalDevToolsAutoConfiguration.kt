package com.wanna.boot.devtools.autoconfigure

import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.boot.devtools.classpath.ClassPathChangedEvent
import com.wanna.boot.devtools.classpath.ClassPathFileSystemWatcher
import com.wanna.boot.devtools.classpath.ClassPathRestartStrategy
import com.wanna.boot.devtools.classpath.PatternClassPathRestartStrategy
import com.wanna.boot.devtools.filewatch.FileSystemWatcher
import com.wanna.boot.devtools.filewatch.FileSystemWatcherFactory
import com.wanna.boot.devtools.filewatch.SnapshotStateRepository
import com.wanna.boot.devtools.restart.ConditionalOnInitializedRestarter
import com.wanna.boot.devtools.restart.Restarter
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.util.StringUtils
import java.io.File

/**
 * 本地的DevTools的自动配置类, 提供本地的热部署的实现
 */
@ConditionalOnInitializedRestarter  // Conditional On Restarter has been initialized
@Configuration(proxyBeanMethods = false)
open class LocalDevToolsAutoConfiguration {

    /**
     * 本地的DevTools的配置类, 导入本地DevTools的支持的相关Spring Bean
     *
     * @see FileSystemWatcherFactory
     * @see ClassPathFileSystemWatcher
     */
    @EnableConfigurationProperties([DevToolsProperties::class])
    @Configuration(proxyBeanMethods = false)
    open class RestartConfiguration {

        /**
         * 自动注入DevTools的配置信息
         */
        @Autowired
        private lateinit var properties: DevToolsProperties

        /**
         * 给Spring BeanFactory添加一个处理[ClassPathChangedEvent]的监听器,
         * 负责在ClassPath下的文件发生变化时提供去重启整个SpringApplication
         *
         * @return 处理[ClassPathChangedEvent]的ApplicationListener
         */
        @Bean
        open fun restartingClassPathChangedEventListener(factory: FileSystemWatcherFactory): ApplicationListener<ClassPathChangedEvent> {
            // Note: 这里不要使用lambda表达式, 可以使用匿名内部类, 因为lambda表达式会导致泛型推断不出来...
            return RestartingClassPathChangedEventListener(factory)
        }

        /**
         * 给Spring BeanFactory当中去添加一个ClassPath下的文件系统的Watcher, 监听文件的变化
         *
         * @param factory 提供FileSystemWatcher的Factory
         * @param pathRestartStrategy Restart策略(只有在改变的文件符合该策略的规则时才去进行重启)
         * @return ClassPathFileSystemWatcher
         */
        @Bean
        open fun classFilePathSystemWatcher(
            factory: FileSystemWatcherFactory, pathRestartStrategy: ClassPathRestartStrategy
        ): ClassPathFileSystemWatcher {
            val urls =
                Restarter.getInstance()!!.getInitialUrls() ?: throw IllegalStateException("无法获取到InitialUrls")
            val classPathFileSystemWatcher =
                ClassPathFileSystemWatcher(factory.getFileSystemWatcher(), urls, pathRestartStrategy)

            // 对于LocalDevTools来说, 当ClassPathChangedEvent发布时, 需要去重启Watcher
            classPathFileSystemWatcher.stopWatcherOnRestart = true
            return classPathFileSystemWatcher
        }

        /**
         * FileSystemWatcher的Factory, 负责提供FileSystemWatcher
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

                // 添加SourceDirectory
                properties.restart.additionalPaths.forEach {
                    fileSystemWatcher.addSourceDirectory(File(it))
                }
                // 如果配置文件当中, 指定了触发的Restart文件的话, 需要添加TriggerFileFilter
                val triggerFile = properties.restart.triggerFile
                if (StringUtils.hasText(triggerFile)) {
                    fileSystemWatcher.setTriggerFilter(TriggerFileFilter(triggerFile!!))
                }
                fileSystemWatcher
            }
        }

        /**
         * 基于路径匹配的重启策略
         */
        @Bean
        @ConditionalOnMissingBean
        open fun patternClassPathRestartStrategy(): PatternClassPathRestartStrategy {
            return PatternClassPathRestartStrategy(properties.restart.exclude)
        }

        /**
         * 监听[ClassPathChangedEvent]事件, 去重新启动SpringApplication
         *
         * @param factory FileSystemWatcherFactory
         */
        private class RestartingClassPathChangedEventListener(private val factory: FileSystemWatcherFactory) :
            ApplicationListener<ClassPathChangedEvent> {
            override fun onApplicationEvent(event: ClassPathChangedEvent) {
                if (event.restartRequired) {
                    Restarter.getInstance()!!.restart(FileWatchingFailureHandler(factory))  // 重启SpringApplication
                }
            }
        }
    }
}