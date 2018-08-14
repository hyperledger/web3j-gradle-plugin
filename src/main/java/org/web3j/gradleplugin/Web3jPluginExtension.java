package org.web3j.gradleplugin;

import java.util.Objects;

import org.gradle.api.Project;

/**
 * web3j extension for plugin configuration.
 */
public class Web3jPluginExtension {

    /**
     * Extension name used in Gradle build files.
     */
    static final String NAME = "web3j";

    private static final String DEFAULT_GENERATED_PACKAGE = "org.web3j.model";

    /**
     * Generated package name for web3j contract wrappers.
     * Accepts a {@link java.text.MessageFormat} string
     * with a unique parameter (i.e. {0} ), formatted as
     * the contract name in lower case.
     */
    private String generatedPackageName;

    /**
     * Base directory for generated Java files.
     */
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
        generatedFilesBaseDir = project.getBuildDir().getAbsolutePath()
                + "/generated/source/web3j/";

        // Use the project's group name in generated package
        final String projectGroup = project.getGroup().toString();
        if (!projectGroup.isEmpty()) {
            generatedPackageName = projectGroup + ".web3j";
        } else {
            generatedPackageName = DEFAULT_GENERATED_PACKAGE;
        }
    }

}
