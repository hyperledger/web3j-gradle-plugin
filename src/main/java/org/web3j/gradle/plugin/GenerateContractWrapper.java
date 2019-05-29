package org.web3j.gradle.plugin;

import java.io.File;

import javax.inject.Inject;

import org.gradle.api.internal.plugins.PluginApplicationException;
import org.web3j.codegen.SolidityFunctionWrapper;
import org.web3j.utils.Files;

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
        final SolidityFunctionWrapper wrapper =
                new SolidityFunctionWrapper(useNativeJavaTypes, addressLength);

        try {
            wrapper.generateJavaFiles(contractName, Files.readString(contractBin),
                    Files.readString(contractAbi), outputDir, packageName);
        } catch (Exception e) {
            throw new PluginApplicationException(Web3jPlugin.ID, e);
        }
    }

}
