package org.web3j.gradleplugin;

import java.io.File;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.Convention;
import org.gradle.api.tasks.SourceSet;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

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

        final Web3jPluginExtension web3jExtension = (Web3jPluginExtension)
                InvokerHelper.getProperty(project, Web3jPluginExtension.NAME);

        final File destFolder = new File(project.getBuildDir(),
                web3jExtension.getGeneratedFilesBaseDir()
                        + "/" + sourceSet.getName() + "/java");

        // Set the task output directory
        task.getOutputs().dir(destFolder);

        // Set the task generated package name
        task.setGeneratedJavaPackageName(web3jExtension.getGeneratedPackageName());
    }

}
