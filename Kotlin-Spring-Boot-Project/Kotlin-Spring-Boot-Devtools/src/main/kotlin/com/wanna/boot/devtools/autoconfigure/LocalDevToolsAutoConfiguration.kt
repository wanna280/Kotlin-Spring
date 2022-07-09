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
import com.wanna.framework.core.util.StringUtils
import java.io.File

@ConditionalOnInitializedRestarter  // Conditional On Restarter has been initialized
@Configuration(proxyBeanMethods = false)
open class LocalDevToolsAutoConfiguration {

    @EnableConfigurationProperties([DevToolsProperties::class])
    @Configuration(proxyBeanMethods = false)
    open class RestartConfiguration {

        @Autowired
        private lateinit var properties: DevToolsProperties

        /**
         * 给Spring BeanFactory添加一个处理ClassPathChangedEvent的监听器，
         * 负责在ClassPath下的文件发生变化时提供去重启整个SpringApplication
         *
         * @return 处理ClassPathChangedEvent的ApplicationListener
         */
        @Bean
        open fun restartingClassPathChangedEventListener(): ApplicationListener<ClassPathChangedEvent> {
            return object : ApplicationListener<ClassPathChangedEvent> {
                override fun onApplicationEvent(event: ClassPathChangedEvent) {
                    if (event.restartRequired) {
                        Restarter.getInstance()!!.restart()  // 重启SpringApplication
                    }
                }
            }
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
            val urls = Restarter.getInstance()!!.getInitialUrls() ?: throw IllegalStateException("无法获取到InitialUrls")
            val classPathFileSystemWatcher =
                ClassPathFileSystemWatcher(factory.getFileSystemWatcher(), urls, pathRestartStrategy)

            // 当重启Application时，重启Watcher
            classPathFileSystemWatcher.stopWatcherOnRestart = true
            return classPathFileSystemWatcher
        }

        /**
         * FileSystemWatcher的Factory，负责提供FileSystemWatcher
         *
         * @see FileSystemWatcher
         * @see FileSystemWatcherFactory
         */
        @Bean
        @ConditionalOnMissingBean
        open fun fileSystemWatcherFactory(): FileSystemWatcherFactory {
            return object : FileSystemWatcherFactory {
                override fun getFileSystemWatcher(): FileSystemWatcher {
                    // 创建FileSystemWatcher
                    val fileSystemWatcher =
                        FileSystemWatcher(
                            true, properties.restart.pollInterval, properties.restart.quietPeriod,
                            SnapshotStateRepository.STATIC
                        )

                    // 添加SourceDirectory
                    properties.restart.additionalPaths.forEach {
                        fileSystemWatcher.addSourceDirectory(File(it))
                    }
                    // 如果配置文件当中，指定了触发的Restart文件的话，需要添加TriggerFileFilter
                    val triggerFile = properties.restart.triggerFile
                    if (StringUtils.hasText(triggerFile)) {
                        fileSystemWatcher.setTriggerFilter(TriggerFileFilter(triggerFile!!))
                    }
                    return fileSystemWatcher
                }
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
    }
}