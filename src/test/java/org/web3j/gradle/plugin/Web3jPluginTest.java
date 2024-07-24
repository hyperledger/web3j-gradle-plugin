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
package org.web3j.gradle.plugin;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Web3jPluginTest {

    private Path testProjectDir;
    private Path buildFile;
    private Path sourceDir;

    @BeforeEach
    public void setup() throws IOException, URISyntaxException {
        testProjectDir = Files.createTempDirectory("testProjectDir");
        buildFile = testProjectDir.resolve("build.gradle");
        URL resource = getClass().getClassLoader().getResource("solidity/StandardToken.sol");
        if (resource != null) {
            sourceDir = Paths.get(resource.toURI()).getParent();
        }
    }

    @Test
    public void generateContractWrappersExcluding() throws IOException {
        String buildFileContent =
                "plugins {\n"
                        + "    id 'org.web3j'\n"
                        + "}\n"
                        + "web3j {\n"
                        + "    generatedPackageName = 'org.web3j.test'\n"
                        + "    excludedContracts = ['Token']\n"
                        + "}\n"
                        + "sourceSets {\n"
                        + "    main {\n"
                        + "        solidity {\n"
                        + "            srcDir '"
                        + sourceDir.toAbsolutePath()
                        + "'\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n"
                        + "repositories {\n"
                        + "    mavenCentral()\n"
                        + "    maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }\n"
                        + "}\n";

        Files.write(buildFile, buildFileContent.getBytes());

        GradleRunner gradleRunner =
                GradleRunner.create()
                        .withProjectDir(testProjectDir.toFile())
                        .withArguments("build")
                        .withPluginClasspath()
                        .forwardOutput();

        BuildResult success = gradleRunner.build();
        assertNotNull(success.task(":generateContractWrappers"));
        assertEquals(TaskOutcome.SUCCESS, success.task(":generateContractWrappers").getOutcome());

        Path web3jContractsDir = testProjectDir.resolve("build/generated/sources/web3j/main/java");
        Path generatedContract = web3jContractsDir.resolve("org/web3j/test/StandardToken.java");
        assertTrue(Files.exists(generatedContract));

        Path excludedContract = web3jContractsDir.resolve("org/web3j/test/Token.java");
        assertFalse(Files.exists(excludedContract));

        BuildResult upToDate = gradleRunner.build();
        assertNotNull(upToDate.task(":generateContractWrappers"));
        assertEquals(
                TaskOutcome.UP_TO_DATE, upToDate.task(":generateContractWrappers").getOutcome());
    }

    @Test
    public void generateContractWrappersIncluding() throws IOException {
        final String buildFileContent =
                "plugins {\n"
                        + "    id 'org.web3j'\n"
                        + "}\n"
                        + "web3j {\n"
                        + "    generatedPackageName = 'org.web3j.test'\n"
                        + "    includedContracts = ['StandardToken']\n"
                        + "    generateBoth = true\n"
                        + "}\n"
                        + "sourceSets {\n"
                        + "    main {\n"
                        + "        solidity {\n"
                        + "            srcDir '"
                        + sourceDir.toAbsolutePath()
                        + "'\n"
                        + "            }\n"
                        + "        }\n"
                        + "    }\n"
                        + "repositories {\n"
                        + "   mavenCentral()\n"
                        + "   maven {\n"
                        + "       url 'https://oss.sonatype.org/content/repositories/snapshots'\n"
                        + "   }\n"
                        + "}\n";

        Files.write(buildFile, buildFileContent.getBytes());

        final GradleRunner gradleRunner =
                GradleRunner.create()
                        .withProjectDir(testProjectDir.toFile())
                        .withArguments("build")
                        .withPluginClasspath()
                        .forwardOutput();

        final BuildResult success = gradleRunner.build();
        assertNotNull(success.task(":generateContractWrappers"));
        assertEquals(SUCCESS, success.task(":generateContractWrappers").getOutcome());

        final Path web3jContractsDir =
                testProjectDir.resolve("build/generated/sources/web3j/main/java");
        final Path generatedContract =
                web3jContractsDir.resolve("org/web3j/test/StandardToken.java");
        assertTrue(Files.exists(generatedContract));

        final Path excludedContract = web3jContractsDir.resolve("org/web3j/test/Token.java");
        assertFalse(Files.exists(excludedContract));

        final BuildResult upToDate = gradleRunner.build();
        assertNotNull(upToDate.task(":generateContractWrappers"));
        assertEquals(UP_TO_DATE, upToDate.task(":generateContractWrappers").getOutcome());
    }

    @Test
    public void generateContractWrappersIncludingGenerateBothFalseUseNativeJava()
            throws IOException {
        final String buildFileContent =
                "plugins {\n"
                        + "    id 'org.web3j'\n"
                        + "}\n"
                        + "web3j {\n"
                        + "    generatedPackageName = 'org.web3j.test'\n"
                        + "    includedContracts = ['StandardToken']\n"
                        + "    useNativeJavaTypes = true\n"
                        + "    generateBoth = false\n"
                        + "}\n"
                        + "sourceSets {\n"
                        + "    main {\n"
                        + "        solidity {\n"
                        + "            srcDir '"
                        + sourceDir.toAbsolutePath()
                        + "'\n"
                        + "            }\n"
                        + "        }\n"
                        + "    }\n"
                        + "repositories {\n"
                        + "   mavenCentral()\n"
                        + "   maven {\n"
                        + "       url 'https://oss.sonatype.org/content/repositories/snapshots'\n"
                        + "   }\n"
                        + "}\n";

        Files.write(buildFile, buildFileContent.getBytes());

        final GradleRunner gradleRunner =
                GradleRunner.create()
                        .withProjectDir(testProjectDir.toFile())
                        .withArguments("build")
                        .withPluginClasspath()
                        .forwardOutput();

        final BuildResult success = gradleRunner.build();
        assertNotNull(success.task(":generateContractWrappers"));
        assertEquals(SUCCESS, success.task(":generateContractWrappers").getOutcome());

        final Path web3jContractsDir =
                testProjectDir.resolve("build/generated/sources/web3j/main/java");
        final Path generatedContract =
                web3jContractsDir.resolve("org/web3j/test/StandardToken.java");
        assertTrue(Files.exists(generatedContract));

        final Path excludedContract = web3jContractsDir.resolve("org/web3j/test/Token.java");
        assertFalse(Files.exists(excludedContract));

        final BuildResult upToDate = gradleRunner.build();
        assertNotNull(upToDate.task(":generateContractWrappers"));
        assertEquals(UP_TO_DATE, upToDate.task(":generateContractWrappers").getOutcome());
    }
}
