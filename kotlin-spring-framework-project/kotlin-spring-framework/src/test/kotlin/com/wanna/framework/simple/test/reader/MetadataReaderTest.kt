package com.wanna.framework.simple.test.reader

import com.wanna.framework.core.type.classreading.SimpleMetadataReaderFactory
import com.wanna.framework.transaction.annotation.Isolation
import com.wanna.framework.transaction.annotation.Transactional

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/18
 */
@Transactional(isolation = Isolation.DEFAULT)
@Ann([Ann2(["1", "2"]), Ann2(["3", "4"])])
class MetadataReaderTest {
}


annotation class Ann(val value: Array<Ann2>)

annotation class Ann2(val value: Array<String>)

fun main() {
    val simpleMetadataReaderFactory = SimpleMetadataReaderFactory()
    val metadataReader =
        simpleMetadataReaderFactory.getMetadataReader("com.wanna.framework.simple.test.reader.MetadataReaderTest")

    val annotationMetadata = metadataReader.annotationMetadata


    val simpleMetadataReaderFactory1 = org.springframework.core.type.classreading.SimpleMetadataReaderFactory()
    val metadataReader1 =
        simpleMetadataReaderFactory1.getMetadataReader("com.wanna.framework.simple.test.reader.MetadataReaderTest")
    val annotationMetadata1 = metadataReader1.annotationMetadata

    println()
}