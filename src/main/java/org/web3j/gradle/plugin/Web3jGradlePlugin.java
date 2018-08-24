package org.web3j.gradle.plugin;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.file.SourceDirectorySetFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSetContainer;

/**
 * Gradle plugin class for web3j code generation from Solidity contracts.
 */
public class Web3jGradlePlugin implements Plugin<Project> {

    private final SourceDirectorySetFactory sourceFactory;

    @Inject
    public Web3jGradlePlugin(final SourceDirectorySetFactory sourceFactory) {
        this.sourceFactory = sourceFactory;
    }

    public void apply(final Project project) {
        project.getPluginManager().apply(JavaPlugin.class);
        project.getExtensions().create(Web3jPluginExtension.NAME,
                Web3jPluginExtension.class, project);

        final SourceSetContainer sourceSets = project.getConvention()
                .getPlugin(JavaPluginConvention.class).getSourceSets();

        final SoliditySourceSetAction sourceSetAction =
                new SoliditySourceSetAction(project, sourceFactory);

        final GenerateJavaAction generateJavaAction = new GenerateJavaAction(project);

        sourceSets.all(sourceSetAction);
        project.afterEvaluate(p -> sourceSets.all(generateJavaAction));
    }

}
