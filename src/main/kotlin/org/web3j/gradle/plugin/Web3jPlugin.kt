/*
 * Copyright 2019 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.web3j.gradle.plugin

import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.StringGroovyMethods.capitalize
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.plugins.PluginApplicationException
import org.gradle.api.plugins.Convention
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceTask
import org.gradle.internal.Describables
import org.web3j.solidity.gradle.plugin.SolidityCompile
import org.web3j.solidity.gradle.plugin.SolidityPlugin
import org.web3j.solidity.gradle.plugin.SoliditySourceSet
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.util.Properties

/** Gradle plugin class for web3j code generation from Solidity contracts.  */
open class Web3jPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.pluginManager.apply(JavaPlugin::class.java)
        target.pluginManager.apply(SolidityPlugin::class.java)
        target.dependencies.add("implementation", "org.web3j:core:$projectVersion")

        registerExtensions(target)

        val sourceSets = target.convention.getPlugin(JavaPluginConvention::class.java).sourceSets
        target.afterEvaluate {
            sourceSets.all { sourceSet: SourceSet ->
                configure(target, sourceSet)
            }
        }
    }

    protected open fun registerExtensions(project: Project) {
        project.extensions.create(Web3jExtension.NAME, Web3jExtension::class.java, project)
    }

    protected open val projectVersion: String
        get() {
            val versionPropsFile = javaClass.classLoader.getResource("version.properties")
            return if (versionPropsFile == null) {
                throw PluginApplicationException(
                        Describables.of("No version.properties file found in the classpath."), null)
            } else {
                try {
                    Properties().apply {
                        load(versionPropsFile.openStream())
                    }.getProperty("version")
                } catch (e: IOException) {
                    throw PluginApplicationException(
                            Describables.of("Could not read version.properties file."), e)
                }
            }
        }

    /**
     * Configures code generation tasks for the Solidity source sets defined in the project (e.g.
     * main, test).
     *
     *
     * The generated task name for the `main` source set will be `
     * generateContractWrappers`, and for `test` `generateTestContractWrappers
    ` * .
     */
    private fun configure(project: Project, sourceSet: SourceSet) {
        val extension = InvokerHelper.getProperty(project, Web3jExtension.NAME) as Web3jExtension
        val outputDir = buildSourceDir(extension, sourceSet)

        // Add source set to the project Java source sets
        sourceSet.java.srcDir(outputDir)

        val srcSetName = if (sourceSet.name == "main") "" else capitalize(sourceSet.name as CharSequence)
        val generateTaskName = "generate" + srcSetName + "ContractWrappers"

        val taskProvider = project.tasks.register(
                generateTaskName,
                GenerateContractWrappers::class.java
        ) { task: GenerateContractWrappers ->
            // Set the sources for the generation task
            task.source = buildSourceDirectorySet(project, sourceSet)
            task.description = "Generates ${sourceSet.name} Java contract wrappers from Solidity ABIs."

            // Set the task output directory
            task.outputs.dir(outputDir)

            // Set the task generated package name, classpath and group
            task.generatedJavaPackageName = extension.generatedPackageName
            task.useNativeJavaTypes = extension.useNativeJavaTypes
            task.group = Web3jExtension.NAME

            // Set task excluded contracts
            task.excludedContracts = extension.excludedContracts
            task.includedContracts = extension.includedContracts

            // Set the contract addresses length (default 160)
            task.addressLength = extension.addressBitLength
            task.dependsOn(
                    project.tasks
                            .withType(SolidityCompile::class.java)
                            .named("compile" + srcSetName + "Solidity"))
        }
        project.tasks.named("compile" + srcSetName + "Java", SourceTask::class.java).configure {
            it.source(taskProvider.get().outputs.files.singleFile)
            it.dependsOn(taskProvider)
        }
    }

    protected fun buildSourceDirectorySet(
        project: Project,
        sourceSet: SourceSet
    ): SourceDirectorySet {
        val displayName = capitalize(sourceSet.name as CharSequence) + " Solidity ABI"
        return project.objects.sourceDirectorySet(sourceSet.name, displayName).apply {
            srcDir(buildOutputDir(sourceSet))
            include("**/*.abi")
        }
    }

    private fun buildSourceDir(extension: Web3jExtension, sourceSet: SourceSet): File {
        if (extension.generatedFilesBaseDir.isEmpty()) {
            throw InvalidUserDataException("Generated web3j package cannot be empty")
        }
        return Paths.get(extension.generatedFilesBaseDir, sourceSet.name, "java").toFile()
    }

    protected fun buildOutputDir(sourceSet: SourceSet?): File {
        val convention = InvokerHelper.getProperty(sourceSet, "convention") as Convention
        val soliditySourceSet = convention.plugins[SoliditySourceSet.NAME] as SoliditySourceSet
        return soliditySourceSet.solidity.outputDir
    }
}
