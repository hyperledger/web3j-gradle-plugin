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
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkerExecutor;

import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.AbiDefinition;

@CacheableTask
public class GenerateContractWrappers extends SourceTask {

    private final WorkerExecutor executor;

    @Input private String generatedJavaPackageName;

    @Input @Optional private Boolean useNativeJavaTypes;

    @Input @Optional private List<String> excludedContracts;

    @Input @Optional private List<String> includedContracts;

    @Input @Optional private Integer addressLength;

    @Inject
    public GenerateContractWrappers(final WorkerExecutor executor) {
        this.executor = executor;
    }

    @TaskAction
    void generateContractWrappers() throws IOException {

        final String outputDir = getOutputs().getFiles().getSingleFile().getAbsolutePath();

        for (final File contractAbi : getSource()) {

            final String contractName = contractAbi.getName().replaceAll("\\.abi", "");

            if (shouldGenerateContract(contractName) && !abiIsEmpty(contractAbi)) {
                final String packageName =
                        MessageFormat.format(
                                getGeneratedJavaPackageName(), contractName.toLowerCase());

                final File contractBin =
                        new File(contractAbi.getParentFile(), contractName + ".bin");

                executor.noIsolation()
                        .submit(
                                GenerateContractWrapper.class,
                                (GenerateContractWrapper.Parameters params) -> {
                                    params.getContractName().set(contractName);
                                    params.getContractBin().set(contractBin);
                                    params.getContractAbi().set(contractAbi);
                                    params.getOutputDir().set(outputDir);
                                    params.getPackageName().set(packageName);
                                    params.getAddressLength().set(addressLength);
                                    params.getUseNativeJavaTypes().set(useNativeJavaTypes);
                                });
            }
        }
    }

    private boolean abiIsEmpty(File abi) throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        AbiDefinition[] abiDefinition = objectMapper.readValue(abi, AbiDefinition[].class);
        return abiDefinition.length == 0;
    }

    private boolean shouldGenerateContract(final String contractName) {

        if (includedContracts == null || includedContracts.isEmpty()) {
            return excludedContracts == null || !excludedContracts.contains(contractName);
        } else {
            return includedContracts.contains(contractName);
        }
    }

    // Getters and setters
    public String getGeneratedJavaPackageName() {
        return generatedJavaPackageName;
    }

    public void setGeneratedJavaPackageName(final String generatedJavaPackageName) {
        this.generatedJavaPackageName = generatedJavaPackageName;
    }

    public Boolean getUseNativeJavaTypes() {
        return useNativeJavaTypes;
    }

    public void setUseNativeJavaTypes(final Boolean useNativeJavaTypes) {
        this.useNativeJavaTypes = useNativeJavaTypes;
    }

    public List<String> getExcludedContracts() {
        return excludedContracts;
    }

    public void setExcludedContracts(final List<String> excludedContracts) {
        this.excludedContracts = excludedContracts;
    }

    public List<String> getIncludedContracts() {
        return includedContracts;
    }

    public void setIncludedContracts(final List<String> includedContracts) {
        this.includedContracts = includedContracts;
    }

    public Integer getAddressLength() {
        return addressLength;
    }

    public void setAddressLength(final Integer addressLength) {
        this.addressLength = addressLength;
    }
}
