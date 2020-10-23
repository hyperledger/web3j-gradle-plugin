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

import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.web3j.codegen.SolidityFunctionWrapperGenerator
import org.web3j.gradle.plugin.GenerateContractWrapper.Parameters
import java.io.File

abstract class GenerateContractWrapper : WorkAction<Parameters> {
    override fun execute() {
        val typesFlag = if (parameters.useNativeJavaTypes.get()) "--javaTypes" else "--solidityTypes"
        SolidityFunctionWrapperGenerator.main(arrayOf(
                "--abiFile",
                parameters.contractAbi.get().absolutePath,
                "--binFile",
                parameters.contractBin.get().absolutePath,
                "--outputDir",
                parameters.outputDir.get(),
                "--package",
                parameters.packageName.get(),
                "--contractName",
                parameters.contractName.get(),
                "--addressLength", parameters.addressLength.get().toString(),
                typesFlag
        ))
    }

    interface Parameters : WorkParameters {
        val contractName: Property<String>
        val contractBin: Property<File>
        val contractAbi: Property<File>
        val outputDir: Property<String>
        val packageName: Property<String>
        val addressLength: Property<Int>
        val useNativeJavaTypes: Property<Boolean>
    }
}
