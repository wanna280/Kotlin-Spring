/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wanna.boot.loader.jar

import java.security.CodeSigner
import java.security.cert.Certificate
import java.util.jar.JarEntry


/**
 *
 * JarEntry的签名信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/4
 */
class JarEntryCertification(
    private val certificates: Array<Certificate>?,
    private val codeSigners: Array<CodeSigner>?
) {
    fun getCertificates(): Array<Certificate>? = certificates?.clone()

    fun getCodeSigners(): Array<CodeSigner>? = codeSigners?.clone()

    companion object {

        /**
         * 没有签名的JarEntryCertification的常量
         */
        @JvmField
        val NONE = JarEntryCertification(null, null)

        @JvmStatic
        fun from(certifiedEntry: JarEntry?): JarEntryCertification {
            val certificates = certifiedEntry?.certificates
            val codeSigners = certifiedEntry?.codeSigners
            return if (certificates == null && codeSigners == null) {
                NONE
            } else JarEntryCertification(certificates, codeSigners)
        }
    }
}