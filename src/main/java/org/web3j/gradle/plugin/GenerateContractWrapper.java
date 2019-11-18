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
package org.web3j.gradle.plugin;

import java.io.File;
import javax.inject.Inject;

import org.web3j.codegen.SolidityFunctionWrapperGenerator;

public class GenerateContractWrapper implements Runnable {

    private final String contractName;

    private final File contractBin;
    private final File contractAbi;

    private final String outputDir;
    private final String packageName;

    private final int addressLength;

    private final boolean useNativeJavaTypes;

    @Inject
    public GenerateContractWrapper(
            final String contractName,
            final File contractBin,
            final File contractAbi,
            final String outputDir,
            final String packageName,
            final int addressLength,
            final boolean useNativeJavaTypes) {
        this.contractName = contractName;
        this.contractBin = contractBin;
        this.contractAbi = contractAbi;
        this.outputDir = outputDir;
        this.packageName = packageName;
        this.addressLength = addressLength;
        this.useNativeJavaTypes = useNativeJavaTypes;
    }

    @Override
    public void run() {
        final String typesFlag = useNativeJavaTypes ? "--javaTypes" : "--solidityTypes";

        SolidityFunctionWrapperGenerator.main(
                new String[] {
                    "--abiFile",
                    contractAbi.getAbsolutePath(),
                    "--binFile",
                    contractBin.getAbsolutePath(),
                    "--outputDir",
                    outputDir,
                    "--package",
                    packageName,
                    "--contractName",
                    contractName,
                    "--addressLength",
                    String.valueOf(addressLength),
                    typesFlag
                });
    }
}
