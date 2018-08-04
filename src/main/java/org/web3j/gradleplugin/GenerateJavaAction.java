package org.web3j.gradleplugin;

import java.io.File;
import java.util.Iterator;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.Convention;
import org.gradle.api.tasks.SourceSet;

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

        final String srcSetName = StringGroovyMethods.capitalize(sourceSet.getName());

        final GenerateJavaTask task = project.getTasks().create(
                "generate" + srcSetName + "Java", GenerateJavaTask.class);

        final Iterator<File> sourceFiles = soliditySourceSet.getSolidity().iterator();

        if (sourceFiles.hasNext()) {
            final File sourcesDir = sourceFiles.next().getParentFile();
            task.setSolidityContractsFolder(sourcesDir.getAbsolutePath());

            final File destFolder = new File(project.getBuildDir(),
                    "resources/" + sourceSet.getName() + "/solidity");

            task.setGeneratedJavaDestFolder(destFolder.getAbsolutePath());

            final String projectGroup = project.getGroup().toString();
            if (!projectGroup.isEmpty()) {
                task.setGeneratedJavaPackageName(projectGroup + ".web3j");
            }
        }
    }

}
