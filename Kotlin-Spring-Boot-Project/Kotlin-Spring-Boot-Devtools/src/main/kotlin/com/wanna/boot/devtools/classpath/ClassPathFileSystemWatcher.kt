package com.wanna.boot.devtools.classpath

import com.wanna.boot.devtools.filewatch.FileSystemWatcher
import com.wanna.boot.devtools.restart.Restarter
import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.beans.factory.support.DisposableBean
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import java.net.URL

/**
 * ClassPath下的文件系统的Watcher，负责包装一个FileSystemWatcher去完成对本地的输出目录去进行Watch；
 * 我们不必去监控jar包的改变，我们要做的，只是监控本地的输出目录当中的文件变化情况
 *
 * @param fileSystemWatcher FileSystemWatcher
 * @param urls 候选的要去进行监控的URL列表(jar包/outputPath)
 * @param restartStrategy 重启策略，用来判断某个文件发生变更时，是否应该重启应用？
 */
class ClassPathFileSystemWatcher(
    private val fileSystemWatcher: FileSystemWatcher,
    private val urls: Array<URL>,
    private val restartStrategy: ClassPathRestartStrategy?
) : InitializingBean, DisposableBean, ApplicationContextAware {

    // 在重启时，是否需要stopWatcher？
    var stopWatcherOnRestart: Boolean = true

    private var applicationContext: ApplicationContext? = null

    init {
        // 我们需要往FileSystemWatcher当中去添加去监控的SourceDirectory
        // 我们将给定的URL当中的所有的"目录"去添加到FileSystemWatcher当中(对于jar包我们无需关注)
        this.fileSystemWatcher.addSourceDirectories(ClassPathDirectories(urls))
    }

    /**
     * 在初始化Bean时，需要去启动FileSystemWatcher，去检测指定的目录下的文件的变化
     *
     * @see FileSystemWatcher
     * @see ClassPathRestartStrategy
     */
    override fun afterPropertiesSet() {
        // 如果指定了restart策略的话，我们需要去添加一个Listener去处理文件发生变化时去重启Application
        if (this.restartStrategy != null) {
            // 在重启时，是否需要去重启FileSystemWatcher？
            var watcherToStop: FileSystemWatcher? = null
            if (stopWatcherOnRestart) {
                watcherToStop = this.fileSystemWatcher
            }

            // 添加一个监听ClassPath下的变更的监听器
            // 当ClassPath下的文件发生变更时，会自动回调该监听器
            this.fileSystemWatcher.addListener(
                ClassPathFileChangeListener(
                    applicationContext!!,
                    this.restartStrategy,
                    watcherToStop
                )
            )
        }
        // 启动FileSystemWatcher，开始对文件去进行监控
        this.fileSystemWatcher.start()
    }

    /**
     * 来自Spring的DisposableBean的destroy方法，
     * 在SpringBeanFactory关闭时，我们需要去关闭FileSystemWatcher
     *
     * @see FileSystemWatcher.stop
     */
    override fun destroy() {
        this.fileSystemWatcher.stop()
    }

    /**
     * Spring Application自动设置ApplicationContext
     */
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }
}