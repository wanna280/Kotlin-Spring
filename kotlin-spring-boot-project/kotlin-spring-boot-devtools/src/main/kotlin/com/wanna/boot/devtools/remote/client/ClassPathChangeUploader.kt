package com.wanna.boot.devtools.remote.client

import com.wanna.boot.devtools.classpath.ClassPathChangedEvent
import com.wanna.boot.devtools.filewatch.ChangedFile
import com.wanna.boot.devtools.restart.classloader.ClassLoaderFile
import com.wanna.boot.devtools.restart.classloader.ClassLoaderFiles
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.http.client.ClientHttpRequestFactory
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.ObjectOutputStream
import java.net.URL

/**
 * RemoteClient的ClassPath发生改变的Uploader, 负责将本地的文件的变更上传给RemoteServer;
 * 负责接收本地的文件的变更情况(ClassPathChangedEvent), 并将封装成为ClassLoaderFiles,
 * 并完成序列化, 并将数据写入到RequestBody, 并该序列化完成的数据直接去上传给RemoteServer
 *
 * @see ClassPathChangedEvent
 */
open class ClassPathChangeUploader(url: String, private val clientHttpRequestFactory: ClientHttpRequestFactory) :
    ApplicationListener<ClassPathChangedEvent> {
    // RemoteServer的URI
    private var uri = URL(url).toURI()

    override fun onApplicationEvent(event: ClassPathChangedEvent) {
        // 将本地的发生变更的文件列表, 转换成为ClassLoaderFiles
        val classLoaderFiles = getClassLoaderFiles(event)

        // 将ClassLoaderFiles使用JDK的序列化方式, 直接去序列化成为ByteArray
        val content = serialize(classLoaderFiles)

        // 执行真正地上传, 将ClassLoaderFiles对象, 直接上传给RemoteServer
        performUpload(classLoaderFiles, content)
    }

    /**
     * 使用ObjectOutputStream(JDK的序列化方式), 去将ClassLoaderFiles序列化成为ByteArray
     *
     * @param classLoaderFiles 待序列化的ClassLoaderFiles
     * @return 序列化完成的ByteArray
     */
    private fun serialize(classLoaderFiles: ClassLoaderFiles): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        ObjectOutputStream(byteArrayOutputStream).use { it.writeObject(classLoaderFiles) }
        return byteArrayOutputStream.toByteArray()
    }

    /**
     * 从event的改变的文件列表(changeSet)当中, 获取出来改变的文件, 并包装成为ClassLoaderFiles
     *
     * @param event ClassPathChangedEvent
     * @return 转换之后的ClassLoaderFiles
     */
    private fun getClassLoaderFiles(event: ClassPathChangedEvent): ClassLoaderFiles {
        val classLoaderFiles = ClassLoaderFiles()
        event.changeSet.forEach {
            val directoryName = it.sourceDirectory.absolutePath
            it.files.forEach { file ->
                classLoaderFiles.addFile(directoryName, file.getRelativeName(), asClassLoaderFile(file))
            }
        }
        return classLoaderFiles
    }

    /**
     * 将一个发生变更的文件(ChangedFile)转换为ClassLoaderFile, 方便上传给RemoteServer
     *
     * @param changedFile changedFile
     * @return 将ChangedFile转换为ClassLoaderFile
     */
    private fun asClassLoaderFile(changedFile: ChangedFile): ClassLoaderFile {
        // 获取变更的类型(ADD/DELETE/MODIFY), (ChangedFile.Type->ClassLoaderFile.Kind)
        val type = when (changedFile.type) {
            ChangedFile.Type.ADD -> ClassLoaderFile.Kind.ADDED
            ChangedFile.Type.DELETE -> ClassLoaderFile.Kind.DELETED
            ChangedFile.Type.MODIFY -> ClassLoaderFile.Kind.MODIFIED
        }
        val fileInputStream = FileInputStream(changedFile.file)
        val content: ByteArray
        fileInputStream.use { content = it.readAllBytes() }
        return ClassLoaderFile(
            content,
            type,
            if (type == ClassLoaderFile.Kind.DELETED) System.currentTimeMillis() else changedFile.file.lastModified()
        )
    }

    /**
     * 执行真正的上传, 将ClassLoaderFiles的数据以RequestBody的方式上传给RemoteServer
     *
     * @param classLoaderFiles ClassLoaderFiles
     * @param bytes 要去进行上传的数据(RequestBody)
     */
    private fun performUpload(classLoaderFiles: ClassLoaderFiles, bytes: ByteArray) {
        val request = clientHttpRequestFactory.createRequest(this.uri, RequestMethod.POST)
        request.getBody().write(bytes)  // write Body
        request.execute()
    }
}