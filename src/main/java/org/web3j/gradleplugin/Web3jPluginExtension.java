package org.web3j.gradleplugin;

import java.util.Objects;

import org.gradle.api.Project;

public class Web3jPluginExtension {

    static final String NAME = "web3j";

    private static final String DEFAULT_GENERATED_PACKAGE = "org.web3j.model";

    private String generatedPackageName;
    private String generatedFilesBaseDir;

    public String getGeneratedPackageName() {
        return generatedPackageName;
    }

    public void setGeneratedPackageName(final String generatedPackageName) {
        Objects.requireNonNull(generatedPackageName);
        this.generatedPackageName = generatedPackageName;
    }

    public String getGeneratedFilesBaseDir() {
        return generatedFilesBaseDir;
    }

    public void setGeneratedFilesBaseDir(final String generatedFilesBaseDir) {
        Objects.requireNonNull(generatedFilesBaseDir);
        this.generatedFilesBaseDir = generatedFilesBaseDir;
    }

    public Web3jPluginExtension(final Project project) {
        generatedFilesBaseDir = "generated/source/web3j/";

        final String projectGroup = project.getGroup().toString();
        if (!projectGroup.isEmpty()) {
            generatedPackageName = projectGroup + ".web3j";
        } else {
            generatedPackageName = DEFAULT_GENERATED_PACKAGE;
        }
    }

}
