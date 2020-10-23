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

import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import org.web3j.abi.datatypes.Address
import java.io.File
import java.text.MessageFormat
import javax.inject.Inject

@CacheableTask
open class GenerateContractWrappers @Inject constructor(
    private val executor: WorkerExecutor
) : SourceTask() {

    @Input
    lateinit var generatedJavaPackageName: String

    @Input
    var useNativeJavaTypes: Boolean = true

    @Input
    @Optional
    lateinit var excludedContracts: List<String>

    @Input
    @Optional
    lateinit var includedContracts: List<String>

    @Input
    var addressLength: Int = Address.DEFAULT_LENGTH / Byte.SIZE_BITS

    @TaskAction
    fun generateContractWrappers() {
        val outputDir = outputs.files.singleFile.absolutePath
        for (contractAbi in source) {
            val contractName = contractAbi.name.replace("\\.abi".toRegex(), "")
            if (shouldGenerateContract(contractName)) {
                val packageName = MessageFormat.format(
                        generatedJavaPackageName, contractName.toLowerCase())
                val contractBin = File(contractAbi.parentFile, "$contractName.bin")
                executor.noIsolation()
                        .submit(GenerateContractWrapper::class.java) {
                            it.contractName.set(contractName)
                            it.contractBin.set(contractBin)
                            it.contractAbi.set(contractAbi)
                            it.outputDir.set(outputDir)
                            it.packageName.set(packageName)
                            it.addressLength.set(addressLength)
                            it.useNativeJavaTypes.set(useNativeJavaTypes)
                        }
            }
        }
    }

    private fun shouldGenerateContract(contractName: String): Boolean {
        return if (includedContracts.isEmpty()) {
            !excludedContracts.contains(contractName)
        } else {
            includedContracts.contains(contractName)
        }
    }
}
