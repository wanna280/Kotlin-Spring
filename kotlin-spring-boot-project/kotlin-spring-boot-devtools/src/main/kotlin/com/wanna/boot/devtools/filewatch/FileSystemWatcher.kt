package com.wanna.boot.devtools.filewatch

import java.io.File
import java.io.FileFilter
import java.util.concurrent.atomic.AtomicInteger

/**
 * 基于文件系统的方式, 观察指定的文件/文件夹的变化情况的Watcher
 *
 * @param daemon Watcher是否要使用daemon线程?
 * @param pollInterval poll的间隔
 * @param quietPeriod quiet的间隔(在扫描完一次目录之后, 需要休息一段时间再去进行继续扫描)
 * @param snapshotStateRepository 存放快照的仓库
 */
class FileSystemWatcher(
    private val daemon: Boolean = true,
    private val pollInterval: Long = DEFAULT_POLL_INTERVAL,
    private val quietPeriod: Long = DEFAULT_QUIET_PERIOD,
    private val snapshotStateRepository: SnapshotStateRepository = SnapshotStateRepository.NONE
) {
    companion object {
        // 默认的轮询时间(ms)
        private const val DEFAULT_POLL_INTERVAL = 1000L

        // 默认的安静的间隔(ms)
        private const val DEFAULT_QUIET_PERIOD = 400L
    }

    // 当文件发生变化时, 需要回调的所有监听器
    private val listeners = ArrayList<FileChangeListener>()

    // Monitor锁
    private val monitor = Any()

    // 负责去进行观察的线程(Watcher线程), 在启动时会自动初始化
    private var watchThread: Thread? = null

    // Watcher要去进行检测是否有发生文件变更的目录信息
    private val directories: MutableMap<File, DirectorySnapshot?> = LinkedHashMap()

    // Watcher剩下的扫描次数(如果为-1, 代表一直扫描; 如果＞0代表剩余的扫描次数, 默认为-1)
    private val remainingScans = AtomicInteger(-1)

    // 触发Restart的FileFilter, 只有符合该Filter的条件的文件才需要Restart(如果不进行指定, 那么任何一个文件的改变, 都将作为触发的条件)
    private var triggerFilter: FileFilter? = null

    /**
     * 检查当前Watcher线程是否已经启动了
     *
     * @throws IllegalStateException 如果一个线程已经启动过了
     */
    @Throws(IllegalStateException::class)
    private fun checkStarted() {
        this.watchThread ?: return  // 如果watcher线程为null, 直接return
        throw IllegalStateException("已经有Watcher线程启动了, 不能再去进行操作")  // 如果已经初始化过watcher线程, 那么丢出异常
    }

    /**
     * 添加Listener, 当文件发生改变时会被自动触发
     *
     * @param listener 你想要添加的Listener
     */
    fun addListener(listener: FileChangeListener) {
        synchronized(this.monitor) {
            checkStarted()
            listeners += listener
        }
    }

    /**
     * 设置触发Restart的FileFilter
     *
     * @param triggerFilter triggerFileFilter
     */
    fun setTriggerFilter(triggerFilter: FileFilter) {
        synchronized(this.monitor) {
            this.triggerFilter = triggerFilter
        }
    }

    /**
     * 添加一个要去进行观察的sourceDirectory
     *
     * @param source sourceDirectory
     * @throws IllegalStateException 如果source不是一个目录
     */
    @Throws(IllegalStateException::class)
    fun addSourceDirectory(source: File) {
        if (!source.isDirectory) {
            throw IllegalStateException("给定source不是一个目录, 而是一个文件")
        }
        synchronized(this.monitor) {
            checkStarted()
            directories[source] = null  // put
        }
    }

    /**
     * 添加多个SourceDirectory
     *
     * @param directories 要去添加的目录列表(必须保证每个File都是Directory)
     * @throws IllegalStateException 如果给定的File列表存在有不是文件夹的情况
     */
    @Throws(IllegalStateException::class)
    fun addSourceDirectories(directories: Iterable<File>) {
        directories.forEach(this::addSourceDirectory)
    }

    /**
     * 启动Watcher线程, 去监控文件的改变情况
     *
     * @see Watcher
     */
    @Suppress("UNCHECKED_CAST")
    fun start() {
        synchronized(this.monitor) {

            // 因为addSourceDirectory时, 只是生成了Key, Value=null, 并未完成填充
            // 因此在这里, 我们需要去填充Value, 从而完成文件夹的缓存列表的初始化
            createOrRestoreInitialSnapshots()

            // 创建一个Watcher线程去负责处理文件的变更情况
            if (this.watchThread == null) {
                val watcher = Watcher(
                    this.remainingScans,
                    ArrayList(this.listeners),
                    this.pollInterval,
                    this.quietPeriod,
                    directories as Map<File, DirectorySnapshot>,
                    snapshotStateRepository,
                    this.triggerFilter
                )
                this.watchThread = Thread(watcher)
                this.watchThread!!.name = "File Watcher"
                this.watchThread!!.isDaemon = daemon
                this.watchThread!!.start()
            }
        }
    }

    /**
     * 创建已经添加的目录的Snapshot列表, 为directories的value当中去进行填值
     * 从而完成初始化工作, 因为在之前注册时DirectorySnapshot被初始化为null
     */
    private fun createOrRestoreInitialSnapshots() {
        @Suppress("UNCHECKED_CAST")
        val restored = this.snapshotStateRepository.store() as Map<File, DirectorySnapshot>?
        this.directories.replaceAll { f, _ -> restored?.get(f) ?: DirectorySnapshot(f) }
    }

    /**
     * 关闭Watcher线程, 停止去进行处理文件的改变情况
     */
    fun stop() = stopAfter(0)

    /**
     * 在完成几次"scan"之后自动退出
     *
     * @param remainingScans 剩余"scan"的次数
     */
    private fun stopAfter(remainingScans: Int) {
        val thread: Thread?  // 记录watchThread
        synchronized(this.monitor) {
            thread = this.watchThread
            this.remainingScans.set(remainingScans)

            // 如果设置的剩余次数<=0, 那么说明别去进行扫描了, 直接把线程interrupt
            if (remainingScans <= 0) {
                thread!!.interrupt()
            }
            this.watchThread = null  // set to Null
        }

        // 如果watchThread线程还没执行完, 那么我当前线程应该等着watchThread执行完再走
        if (thread != null && thread != Thread.currentThread()) {
            try {
                thread.join()
            } catch (ex: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    /**
     * 维护了一个去处理文件的快照的变更的Watcher
     *
     * @param remainingScans 剩余的要去执行"scan"的次数
     * @param listeners 监听文件变化的监听器列表
     * @param pollInterval poll的间隔时间
     * @param quietPeriod 扫描完成之后安静的时间
     * @param directories 目录的快照信息
     * @param triggerFilter 触发Restart的Filter
     */
    private class Watcher(
        private val remainingScans: AtomicInteger,
        private val listeners: List<FileChangeListener>,
        private val pollInterval: Long,
        private val quietPeriod: Long,
        private var directories: Map<File, DirectorySnapshot>,
        private val snapshotStateRepository: SnapshotStateRepository,
        private val triggerFilter: FileFilter?
    ) : Runnable {
        override fun run() {
            var remainingScans = this.remainingScans.get()
            while (remainingScans > 0 || remainingScans == -1) {

                try {
                    if (remainingScans > 0) {
                        this.remainingScans.decrementAndGet()
                    }
                    scan()
                } catch (ex: InterruptedException) {
                    Thread.currentThread().interrupt()  // interrupt
                }
                remainingScans = this.remainingScans.get()
            }
        }

        /**
         * 遍历所有的要去进行扫描的文件夹, 去检查是否有文件夹下的文件内容发生变化?
         *
         * @throws IllegalStateException 如果在睡眠的过程当中被interrupt
         */
        @kotlin.jvm.Throws(InterruptedException::class)
        private fun scan() {
            Thread.sleep(pollInterval - quietPeriod)
            var previous: Map<File, DirectorySnapshot>
            var current: Map<File, DirectorySnapshot> = directories

            // 只要触发的文件还有发生变更, 就一直去进行poll(因为用户一直在写代码呢, 我们得等着用户继续写代码)
            // 如果等了很久了文件都没发生变更了, 那么自动跳出循环(应该是用户已经改好代码了, 可以重启了)
            do {
                previous = current
                current = getCurrentSnapshots()
                Thread.sleep(quietPeriod)  // 睡一会, 起来再去进行继续检查
            } while (isDifferent(previous, current))

            // 如果当前的文件夹下的信息相比最初的文件夹下的信息发生了变化的话, 那么需要更新snapshot
            // 就算是这个过程当中文件出现ABA的情况, 也不应该去进行update(因为完全没有必要更新)
            if (isDifferent(this.directories, current)) {
                updateSnapshots(current.values)
            }
        }

        /**
         * 比较之前的和现在的目录下的的触发文件的内容是否不相同?
         *
         * @param previous 先前的Snapshot信息
         * @param current 当前的Snapshot信息
         * @return 如果确实存在不同, 那么return true; 否则return false
         */
        private fun isDifferent(
            previous: Map<File, DirectorySnapshot>,
            current: Map<File, DirectorySnapshot>
        ): Boolean {
            // 之前和现在的目录列表发生改变了(比如数量变了, 文件夹内容变了), 那么return true
            if (previous.keys != current.keys) {
                return true
            }
            // 遍历所有的目录, 挨个去进行比较, 判断当前文件夹下的触发文件是否发生了变更
            // (如果filter==null, 那么所有的文件都会被当作触发文件)
            previous.forEach { (file, previousSnapshot) ->
                if (!previousSnapshot.equals(current[file], this.triggerFilter)) {
                    return true
                }
            }
            return false
        }

        /**
         * 如果文件夹下的文件信息发生了变化, 那么需要去更新维护的文件夹的Snapshot信息,
         * 并将变更的文件列表, 去告知所有的监听器, 让它们去对文件发生变更的事件去进行处理
         *
         * @param snapshots current DirectorySnapshots
         */
        private fun updateSnapshots(snapshots: Collection<DirectorySnapshot?>) {
            val updated = LinkedHashMap<File, DirectorySnapshot>()
            val changeSet = LinkedHashSet<ChangedFiles>()
            snapshots.filterNotNull().forEach {
                updated[it.directory] = it
                val previous = this.directories[it.directory]
                if (previous != null) {
                    val changedFiles = previous.getChangedFiles(it, this.triggerFilter)
                    if (changedFiles.files.isNotEmpty()) {
                        changeSet += changedFiles
                    }
                }
            }
            // 将目录的信息更新成为目前的最新结果
            this.directories = updated
            this.snapshotStateRepository.save(updated)

            // 如果确实有发生变更的目录, 那么我们需要告知所有的监听器去进行处理
            if (changeSet.isNotEmpty()) {
                fireListeners(changeSet)
            }
        }

        /**
         * 获取当前Watcher要去进行处理的所有目录的DirectorySnapshot信息
         *
         * @return 目录的Snapshot信息(key-Directory File, value-DirectorySnapshot)
         */
        private fun getCurrentSnapshots(): Map<File, DirectorySnapshot> =
            this.directories.keys.map { it to DirectorySnapshot(it) }.toMap(LinkedHashMap())

        /**
         * 触发所有的监听器, 告诉这些监听器, 文件已经发生了变更了
         *
         * @param changeSet 发生改变的文件列表
         */
        private fun fireListeners(changeSet: Set<ChangedFiles>) {
            this.listeners.forEach { it.onChange(changeSet) }
        }
    }
}