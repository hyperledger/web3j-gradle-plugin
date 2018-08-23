package org.web3j.gradle.plugin;

import java.io.File;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.Convention;
import org.gradle.api.tasks.SourceSet;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

/**
 * Gradle action configuring code generation tasks for the
 * Solidity source sets defined in the project (e.g. main, test).
 * <p>
 * For instance, the generated task name for the <code>main</code> source set
 * will be <code>generateMainJava</code> and for <code>test</code>,
 * <code>generateTestJava</code>.
 */
class GenerateJavaAction implements Action<SourceSet> {

    private final Project project;

    GenerateJavaAction(final Project project) {
        this.project = project;
    }

    @Override
    public void execute(final SourceSet sourceSet) {

        final Convention convention = (Convention)
                InvokerHelper.getProperty(sourceSet, "convention");

        final SoliditySourceSet soliditySourceSet = (SoliditySourceSet)
                convention.getPlugins().get(SoliditySourceSet.NAME);

        final String srcSetName = capitalize(sourceSet.getName());

        final GenerateJavaTask task = project.getTasks().create(
                "generate" + srcSetName + "Java", GenerateJavaTask.class);

        // Set the sources for the generation task
        task.setSource(soliditySourceSet.getSolidity());

        final Web3jPluginExtension extension = (Web3jPluginExtension)
                InvokerHelper.getProperty(project, Web3jPluginExtension.NAME);

        final File destFolder = new File(extension.getGeneratedFilesBaseDir()
                + "/" + sourceSet.getName() + "/java");

        // Add source set to the project Java source sets
        sourceSet.getJava().srcDir(destFolder);

        // Set the task output directory
        task.getOutputs().dir(destFolder);

        // Set the task generated package name
        task.setGeneratedJavaPackageName(extension.getGeneratedPackageName());
    }

}
