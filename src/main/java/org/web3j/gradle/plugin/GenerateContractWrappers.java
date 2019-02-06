package org.web3j.gradle.plugin;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.IsolationMode;
import org.gradle.workers.WorkerExecutor;
import org.web3j.codegen.SolidityFunctionWrapper;

public class GenerateContractWrappers extends SourceTask {

    private final WorkerExecutor executor;

    @Input
    private String generatedJavaPackageName;

    @Input
    @Optional
    private Boolean useNativeJavaTypes;

    @Input
    @Optional
    private List<String> excludedContracts;

    @Inject
    public GenerateContractWrappers(final WorkerExecutor executor) {
        this.executor = executor;
    }

    @TaskAction
    @SuppressWarnings("unused")
    void generateContractWrappers() {

        final String outputDir = getOutputs().getFiles().getSingleFile().getAbsolutePath();

        for (final File contractAbi : getSource()) {

            final String contractName = contractAbi.getName()
                    .replaceAll("\\.abi", "");

            if (excludedContracts == null || !excludedContracts.contains(contractName)) {
                final String packageName = MessageFormat.format(
                        getGeneratedJavaPackageName(), contractName.toLowerCase());

                final File contractBin = new File(contractAbi.getParentFile(), contractName + ".bin");

                executor.submit(GenerateContractWrapper.class, configuration -> {
                    configuration.setIsolationMode(IsolationMode.NONE);
                    configuration.setParams(contractName, contractBin,
                            contractAbi, outputDir, packageName,
                            getUseNativeJavaTypes());
                });
            }
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


}
