package org.web3j.gradleplugin;

import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.codegen.SolidityFunctionWrapper;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;


public class GenerateJavaTask extends DefaultTask {

    private static final Logger log = LoggerFactory.getLogger(GenerateJavaTask.class);

    private static final String DEFAULT_INCLUDE = "**/*.sol";
    private static final String DEFAULT_GENERATED_PACKAGE = "org.web3j.model";
    private static final String DEFAULT_GENERATED_DEST_FOLDER = "src/main/java";
    private static final String DEFAULT_SOLIDITY_SOURCES = "src/main/resources";
    private static final boolean nativeJavaType = true;

    @Input
    private String generatedJavaPackageName = DEFAULT_GENERATED_PACKAGE;

    @Input
    private String generatedJavaDestFolder = DEFAULT_GENERATED_DEST_FOLDER;

    @Input
    private String solidityContractsFolder = DEFAULT_SOLIDITY_SOURCES;

    private FileSet soliditySourceFiles = new FileSet();


    @TaskAction
    void actionOnAllContracts() throws Exception {



        log.info("\tPrint generatedJavaPackageName: " + generatedJavaPackageName );
        log.info("\tPrint generatedJavaDestFolder: " + generatedJavaDestFolder );
        log.info("\tPrint solidityContractsFolder: " + solidityContractsFolder);


        soliditySourceFiles.setDirectory(solidityContractsFolder);
        soliditySourceFiles.setIncludes(Collections.singletonList(DEFAULT_INCLUDE));

        for (String contractPath : new FileSetManager().getIncludedFiles(soliditySourceFiles)){
            log.info("\tAction on contract '" + solidityContractsFolder + "/" + contractPath + "'" );
            actionOnOneContract(solidityContractsFolder + "/" + contractPath);
        }
    }

    private void actionOnOneContract(String contractPath) throws Exception {
        Map<String, Map<String, String>> contracts = getCompiledContract(contractPath);
        if (contracts == null) {
            log.warn("\tNo Contract found for file '" + contractPath + "'");
            return;
        }
        for (String contractName : contracts.keySet()) {
            try {
                log.info("\tTry to build java class for contract '" + contractName + "'" );
                generateJavaClass(contracts, contractName);
                log.info("\tBuilt Class for contract '" + contractName + "'");
            } catch (Exception e) {
                log.error("Could not build java class for contract '" + contractName + "'", e);
            }
        }
    }

    private Map<String, Map<String, String>> getCompiledContract(String contractPath) throws Exception {

        File f = new File(contractPath);
        if(!f.exists() || f.isDirectory()) {
            return null;
        }

        String result = compileSolidityContract(contractPath);
        // TODO: for some reason a stdin is added to the contract name, removing it the ugly way for now
        result = result.replaceAll("<stdin>:", "");

        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            String script = "JSON.parse(JSON.stringify(" + result + "))";
            Map<String, Object> json = (Map<String, Object>) engine.eval(script);
            return (Map<String, Map<String, String>>) json.get("contracts");
        } catch (ScriptException e) {
            throw new Exception("Could not parse SolC result", e);
        }
    }

    private String compileSolidityContract(String contractPath) throws Exception {
        try {
            byte[] contract = Files.readAllBytes(Paths.get(contractPath));
            SolidityCompiler.Result result = SolidityCompiler.getInstance().compileSrc(
                    contract,
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

    private void generateJavaClass(Map<String, Map<String, String>> result, String contractName) throws IOException, ClassNotFoundException {

        // create the destination repo for contracts
        createJavaFolders();

        new SolidityFunctionWrapper(nativeJavaType).generateJavaFiles(
                contractName,
                result.get(contractName).get("bin"),
                result.get(contractName).get("abi"),
                generatedJavaDestFolder,
                generatedJavaPackageName);
    }

    private void createJavaFolders() throws IOException {
        String currentDir = System.getProperty("user.dir");
        String packageFolders = generatedJavaPackageName.replace(".", "/");
        log.info("\tCreation of folders: " + currentDir + "/" + generatedJavaDestFolder + "/" + packageFolders);
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
