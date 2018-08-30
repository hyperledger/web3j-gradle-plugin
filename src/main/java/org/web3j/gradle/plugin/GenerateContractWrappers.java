package org.web3j.gradle.plugin;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;

import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

public class GenerateContractWrappers extends SourceTask {

    @Input
    private String generatedJavaPackageName;

    @Input
    private FileCollection classpath;

    @TaskAction
    @SuppressWarnings("unused")
    void generateContractWrappers() {
        for (final File contractAbi : getSource()) {

            final String contractName = contractAbi.getName()
                    .replaceAll("\\.abi", "");

            final String packageName = MessageFormat.format(
                    getGeneratedJavaPackageName(), contractName.toLowerCase());

            final String contractDir = contractAbi.getParentFile().getAbsolutePath();

            getProject().javaexec(javaExecSpec -> {
                javaExecSpec.setMain("org.web3j.console.Runner");
                javaExecSpec.setClasspath(getClasspath());
                javaExecSpec.setArgs(Arrays.asList("solidity", "generate",
                        contractDir + "/" + contractName + ".bin",
                        contractAbi.getAbsolutePath(),
                        "-o", getOutputs().getFiles().getSingleFile().getAbsolutePath(),
                        "-p", packageName
                ));
            });
        }
    }

    // Getters and setters
    public String getGeneratedJavaPackageName() {
        return generatedJavaPackageName;
    }

    public void setGeneratedJavaPackageName(final String generatedJavaPackageName) {
        this.generatedJavaPackageName = generatedJavaPackageName;
    }

    public FileCollection getClasspath() {
        return classpath;
    }

    public void setClasspath(final FileCollection classpath) {
        this.classpath = classpath;
    }

}
