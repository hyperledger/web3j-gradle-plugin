package org.web3j.gradleplugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.web3j.codegen.SolidityFunctionWrapper;


public class GenerateJavaTask extends DefaultTask {

    private static final String DEFAULT_INCLUDE = "**/*.sol";
    private static final String DEFAULT_GENERATED_PACKAGE = "org.web3j.model";
    private static final String DEFAULT_GENERATED_DEST_FOLDER = "src/main/java";
    private static final String DEFAULT_SOLIDITY_SOURCES = "src/main/resources";
    private static final boolean NATIVE_JAVA_TYPE = true;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Input
    private String generatedJavaPackageName = DEFAULT_GENERATED_PACKAGE;

    @Input
    private String generatedJavaDestFolder = DEFAULT_GENERATED_DEST_FOLDER;

    @Input
    private String solidityContractsFolder = DEFAULT_SOLIDITY_SOURCES;

    private FileSet soliditySourceFiles = new FileSet();


    @TaskAction
    void actionOnAllContracts() throws Exception {

        soliditySourceFiles.setDirectory(solidityContractsFolder);
        soliditySourceFiles.setIncludes(Collections.singletonList(DEFAULT_INCLUDE));

        for (String contractPath : new FileSetManager().getIncludedFiles(soliditySourceFiles)) {
            getProject().getLogger().info("\tAction on contract '" + solidityContractsFolder + "/" + contractPath + "'");
            actionOnOneContract(solidityContractsFolder + "/" + contractPath);
        }
    }

    private void actionOnOneContract(String contractPath) throws Exception {
        Map<String, Map<String, String>> contracts = getCompiledContract(contractPath);
        if (contracts == null) {
            getProject().getLogger().warn("\tNo Contract found for file '" + contractPath + "'");
            return;
        }
        for (String contractName : contracts.keySet()) {
            try {
                getProject().getLogger().info("\tTry to build java class for contract '" + contractName + "'");
                generateJavaClass(contracts, contractName);
                getProject().getLogger().info("\tBuilt Class for contract '" + contractName + "'");
            } catch (Exception e) {
                getProject().getLogger().error("Could not build java class for contract '" + contractName + "'", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, String>> getCompiledContract(String contractPath)
            throws Exception {

        File contractFile = new File(contractPath);
        if (!contractFile.exists() || contractFile.isDirectory()) {
            return Collections.emptyMap();
        }

        String result = compileSolidityContract(contractFile);
        // TODO: for some reason a stdin is added to the contract name, removing it the ugly way for now
        result = result.replaceAll("<stdin>:", "");

        Map<String, Object> json = (Map<String, Object>)
                OBJECT_MAPPER.readValue(result, Map.class);

        return (Map<String, Map<String, String>>) json.get("contracts");
    }

    private String compileSolidityContract(File contractFile) throws Exception {
        try {
            SolidityCompiler.Result result = SolidityCompiler.getInstance().compileSrc(
                    contractFile,
                    true,
                    true,
                    SolidityCompiler.Options.ABI,
                    SolidityCompiler.Options.BIN,
                    SolidityCompiler.Options.INTERFACE,
                    SolidityCompiler.Options.METADATA
            );
            if (result.isFailed()) {
                throw new Exception("Could not compile solidity files\n" + result.errors);
            }

            return result.output;
        } catch (IOException ioException) {
            throw new Exception("Could not compile files", ioException);
        }
    }

    private void generateJavaClass(
            Map<String, Map<String, String>> result,
            String contractName) throws IOException, ClassNotFoundException {

        // create the destination repo for contracts
        createJavaDestinationFolders();

        new SolidityFunctionWrapper(NATIVE_JAVA_TYPE).generateJavaFiles(
                contractName.split(":")[1],
                result.get(contractName).get("bin"),
                result.get(contractName).get("abi"),
                generatedJavaDestFolder,
                generatedJavaPackageName);
    }

    private void createJavaDestinationFolders() throws IOException {
        String currentDir = System.getProperty("user.dir");
        String packageFolders = generatedJavaPackageName.replace(".", "/");
        getProject().getLogger().info("\tCreation of folders: " + currentDir + "/" + generatedJavaDestFolder + "/" + packageFolders);
        Files.createDirectories(Paths.get(currentDir + "/" + generatedJavaDestFolder + "/" + packageFolders));
    }


    // Getters and setters
    public String getGeneratedJavaPackageName() {
        return generatedJavaPackageName;
    }

    public void setGeneratedJavaPackageName(String generatedJavaPackageName) {
        this.generatedJavaPackageName = generatedJavaPackageName;
    }

    public String getGeneratedJavaDestFolder() {
        return generatedJavaDestFolder;
    }

    public void setGeneratedJavaDestFolder(String generatedJavaDestFolder) {
        this.generatedJavaDestFolder = generatedJavaDestFolder;
    }

    public String getSolidityContractsFolder() {
        return solidityContractsFolder;
    }

    public void setSolidityContractsFolder(String solidityContractsFolder) {
        this.solidityContractsFolder = solidityContractsFolder;
    }
}
