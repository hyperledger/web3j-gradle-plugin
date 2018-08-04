package org.web3j.gradleplugin;

import java.io.File;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.internal.file.SourceDirectorySetFactory;
import org.gradle.api.plugins.Convention;
import org.gradle.api.tasks.SourceSet;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

/**
 * Add default source set for Solidity.
 */
class SoliditySourceSetAction implements Action<SourceSet> {

    private final Project project;
    private final SourceDirectorySetFactory sourceFactory;

    SoliditySourceSetAction(
            final Project project,
            final SourceDirectorySetFactory sourceFactory) {
        this.project = project;
        this.sourceFactory = sourceFactory;
    }

    @Override
    public void execute(final SourceSet sourceSet) {

        final DefaultSoliditySourceSet soliditySourceSet =
                new DefaultSoliditySourceSet(capitalize(sourceSet.getName()), sourceFactory);

        final Convention convention = (Convention)
                InvokerHelper.getProperty(sourceSet, "convention");

        convention.getPlugins().put(SoliditySourceSet.NAME, soliditySourceSet);

        final File defaultSrcDir = new File(project.getBuildDir(),
                "src/" + sourceSet.getName() + "/solidity");

        soliditySourceSet.getSolidity().srcDir(defaultSrcDir);
        sourceSet.getAllJava().source(soliditySourceSet.getSolidity());
        sourceSet.getAllSource().source(soliditySourceSet.getSolidity());
    }

}
