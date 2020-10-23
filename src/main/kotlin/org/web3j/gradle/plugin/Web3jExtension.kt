/*
 * Copyright 2019 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.web3j.gradle.plugin

import org.gradle.api.Project
import org.web3j.abi.datatypes.Address

/** web3j extension for plugin configuration.  */
open class Web3jExtension(project: Project) {
    /**
     * Generated package name for web3j contract wrappers. Accepts a [java.text.MessageFormat]
     * string with a unique parameter (i.e. {0} ), formatted as the contract name in lower case.
     */
    open var generatedPackageName: String = getDefaultGeneratedPackageName(project)

    /** Base directory for generated Java files.  */
    var generatedFilesBaseDir = project.buildDir.absolutePath + "/generated/sources/" + NAME

    /** Generate smart contract wrappers using native Java types.  */
    var useNativeJavaTypes: Boolean = true

    /** Excluded contract names from wrapper generation.  */
    var excludedContracts: List<String> = emptyList()

    /** Included contract names from wrapper generation.  */
    var includedContracts: List<String> = emptyList()

    /** Bit length for network addresses.  */
    var addressBitLength: Int = Address.DEFAULT_LENGTH / java.lang.Byte.SIZE

    protected open fun getDefaultGeneratedPackageName(project: Project): String {
        val defaultPackageName: String
        val projectGroup = project.group.toString()
        defaultPackageName = if (projectGroup.isNotEmpty()) {
            "$projectGroup.$NAME"
        } else {
            DEFAULT_GENERATED_PACKAGE
        }
        return defaultPackageName
    }

    companion object {
        /** Extension name used in Gradle build files.  */
        const val NAME = "web3j"

        private const val DEFAULT_GENERATED_PACKAGE = "org.web3j.{0}"
    }
}
