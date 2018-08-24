package org.web3j.gradle.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.solidity.compiler.SolidityCompiler.Options;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.web3j.codegen.SolidityFunctionWrapper;

import static java.text.MessageFormat.format;

public class GenerateJavaTask extends SourceTask {

    private static final boolean NATIVE_JAVA_TYPE = true;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Input
    private String generatedJavaPackageName;

    @TaskAction
    @SuppressWarnings("unused")
    void actionOnAllContracts() throws Exception {
        for (final File contractFile : getSource()) {
            getProject().getLogger().info("\tAction on contract '"
                    + contractFile.getAbsolutePath() + "'");
            actionOnOneContract(contractFile);
        }
    }

    private void actionOnOneContract(final File contractFile) throws Exception {
        final Map<String, Map<String, String>> contracts = getCompiledContract(contractFile);
        for (final String contractName : contracts.keySet()) {
            try {
                getProject().getLogger().info("\tTry to build java class for contract '" + contractName + "'");
                generateJavaClass(contracts, contractName);
                getProject().getLogger().info("\tBuilt Class for contract '" + contractName + "'");
            } catch (final Exception e) {
                getProject().getLogger().error("Could not build java class for contract '" + contractName + "'", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, String>> getCompiledContract(final File contractFile)
            throws Exception {

        final String result = compileSolidityContract(contractFile)
                // TODO: for some reason a stdin is added to the contract name,
                // removing it the ugly way for now
                .replaceAll("<stdin>:", "");

        final Map<String, Object> json = (Map<String, Object>)
                OBJECT_MAPPER.readValue(result, Map.class);

        return (Map<String, Map<String, String>>) json.get("contracts");
    }

    private String compileSolidityContract(final File contractFile) throws Exception {

        final SolidityCompiler.Result result = SolidityCompiler.getInstance().compileSrc(
                contractFile,
                true,
                true,
                Options.ABI,
                Options.BIN,
                Options.INTERFACE,
                Options.METADATA
        );
        if (result.isFailed()) {
            throw new Exception("Could not compile solidity files: " + result.errors);
        }

        return result.output;
    }

    private void generateJavaClass(
            final Map<String, Map<String, String>> result,
            final String contractNameKey) throws IOException, ClassNotFoundException {

        final String contractName = contractNameKey.split(":")[1];

        new SolidityFunctionWrapper(NATIVE_JAVA_TYPE).generateJavaFiles(
                contractName,
                result.get(contractNameKey).get("bin"),
                result.get(contractNameKey).get("abi"),
                getOutputs().getFiles().getSingleFile().getAbsolutePath(),
                format(generatedJavaPackageName, contractName.toLowerCase()));
    }

    // Getters and setters
    public String getGeneratedJavaPackageName() {
        return generatedJavaPackageName;
    }

    public void setGeneratedJavaPackageName(final String generatedJavaPackageName) {
        this.generatedJavaPackageName = generatedJavaPackageName;
    }

}
