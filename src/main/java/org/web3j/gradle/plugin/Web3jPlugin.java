package org.web3j.gradle.plugin;

import java.io.File;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.DefaultSourceDirectorySet;
import org.gradle.api.internal.file.IdentityFileResolver;
import org.gradle.api.internal.file.collections.DefaultDirectoryFileTreeFactory;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.web3j.solidity.gradle.plugin.CompileSolidity;
import org.web3j.solidity.gradle.plugin.SolidityPlugin;
import org.web3j.solidity.gradle.plugin.SoliditySourceSet;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

/**
 * Gradle plugin class for web3j code generation from Solidity contracts.
 */
public class Web3jPlugin implements Plugin<Project> {

    public void apply(final Project target) {
        target.getPluginManager().apply(JavaPlugin.class);
        target.getPluginManager().apply(SolidityPlugin.class);
        target.getExtensions().create(Web3jExtension.NAME, Web3jExtension.class, target);

        final SourceSetContainer sourceSets = target.getConvention()
                .getPlugin(JavaPluginConvention.class).getSourceSets();

        target.afterEvaluate(p -> sourceSets.all(sourceSet -> configure(target, sourceSet)));
    }

    /**
     * Configures code generation tasks for the Solidity source sets defined in the project
     * (e.g. main, test).
     * <p>
     * The generated task name for the <code>main</code> source set
     * will be <code>generateContractWrappers</code>, and for <code>test</code>
     * <code>generateTestContractWrappers</code>.
     */
    private void configure(final Project project, final SourceSet sourceSet) {

        final Web3jExtension extension = (Web3jExtension)
                InvokerHelper.getProperty(project, Web3jExtension.NAME);

        final File destFolder = buildSourceDir(extension, sourceSet);

        // Add source set to the project Java source sets
        sourceSet.getJava().srcDir(destFolder);

        final String srcSetName = sourceSet.getName().equals("main") ?
                "" : capitalize((CharSequence) sourceSet.getName());

        final String generateTaskName = "generate" + srcSetName + "ContractWrappers";

        final GenerateContractWrappers task = project.getTasks().create(
                generateTaskName, GenerateContractWrappers.class);

        // Set the sources for the generation task
        task.setSource(buildSourceDirectorySet(sourceSet));

        task.setDescription(String.format("Generates web3j contract wrappers for %s source set.",
                sourceSet.getName()));

        // Set the task output directory
        task.getOutputs().dir(destFolder);

        // Set the task generated package name and classpath
        task.setGeneratedJavaPackageName(extension.getGeneratedPackageName());
        task.setClasspath(sourceSet.getRuntimeClasspath());

        task.dependsOn(project.getTasks().withType(CompileSolidity.class)
                .named("compile" + srcSetName + "Solidity"));
    }

    private SourceDirectorySet buildSourceDirectorySet(final SourceSet sourceSet) {

        final SourceDirectorySet directorySet = new DefaultSourceDirectorySet(
                sourceSet.getName(), capitalize(sourceSet.getName()) + " Solidity ABI",
                new IdentityFileResolver(), new DefaultDirectoryFileTreeFactory());

        directorySet.srcDir(buildOutputDir(sourceSet));
        directorySet.include("**/*.abi");

        return directorySet;
    }

    private File buildSourceDir(final Web3jExtension extension, final SourceSet sourceSet) {

        if (extension.getGeneratedFilesBaseDir().isEmpty()) {
            throw new InvalidUserDataException("Generated web3j package cannot be empty");
        }

        return new File(extension.getGeneratedFilesBaseDir()
                + "/" + sourceSet.getName() + "/java");
    }

    private File buildOutputDir(final SourceSet sourceSet) {
        final Convention convention = (Convention)
                InvokerHelper.getProperty(sourceSet, "convention");

        final SoliditySourceSet soliditySourceSet = (SoliditySourceSet)
                convention.getPlugins().get(SoliditySourceSet.NAME);

        return soliditySourceSet.getSolidity().getOutputDir();
    }

}
