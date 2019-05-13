package org.web3j.gradle.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Web3jPluginTest {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    private File buildFile;
    private File sourceDir;

    @Before
    public void setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle");

        final URL resource = getClass().getClassLoader()
                .getResource("solidity/StandardToken.sol");

        sourceDir = new File(resource.getFile()).getParentFile();
    }

    @Test
    public void generateContractWrappersExcluding() throws IOException {
        final String buildFileContent = "plugins {\n" +
                "    id 'org.web3j'\n" +
                "}\n" +
                "web3j {\n" +
                "    generatedPackageName = 'org.web3j.test'\n" +
                "    excludedContracts = ['Token']\n" +
                "}\n" +
                "sourceSets {\n" +
                "    main {\n" +
                "        solidity {\n" +
                "            srcDir {" +
                "                '" + sourceDir.getAbsolutePath() + "'\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "repositories {\n" +
                "   mavenCentral()\n" +
                "}\n";

        Files.write(buildFile.toPath(), buildFileContent.getBytes());

        final GradleRunner gradleRunner = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("build")
                .withPluginClasspath()
                .forwardOutput();

        final BuildResult success = gradleRunner.build();
        assertNotNull(success.task(":generateContractWrappers"));
        assertEquals(SUCCESS, success.task(":generateContractWrappers").getOutcome());

        final File web3jContractsDir = new File(testProjectDir.getRoot(),
                "build/generated/source/web3j/main/java");

        final File generatedContract = new File(web3jContractsDir,
                "org/web3j/test/StandardToken.java");

        assertTrue(generatedContract.exists());

        final File excludedContract = new File(web3jContractsDir,
                "org/web3j/test/Token.java");

        assertFalse(excludedContract.exists());

        final BuildResult upToDate = gradleRunner.build();
        assertNotNull(upToDate.task(":generateContractWrappers"));
        assertEquals(UP_TO_DATE, upToDate.task(":generateContractWrappers").getOutcome());
    }

    @Test
    public void generateContractWrappersIncluding() throws IOException {
        final String buildFileContent = "plugins {\n" +
                "    id 'org.web3j'\n" +
                "}\n" +
                "web3j {\n" +
                "    generatedPackageName = 'org.web3j.test'\n" +
                "    includedContracts = ['StandardToken']\n" +
                "}\n" +
                "sourceSets {\n" +
                "    main {\n" +
                "        solidity {\n" +
                "            srcDir {" +
                "                '" + sourceDir.getAbsolutePath() + "'\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "repositories {\n" +
                "   mavenCentral()\n" +
                "}\n";

        Files.write(buildFile.toPath(), buildFileContent.getBytes());

        final GradleRunner gradleRunner = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("build")
                .withPluginClasspath()
                .forwardOutput();

        final BuildResult success = gradleRunner.build();
        assertNotNull(success.task(":generateContractWrappers"));
        assertEquals(SUCCESS, success.task(":generateContractWrappers").getOutcome());

        final File web3jContractsDir = new File(testProjectDir.getRoot(),
                "build/generated/source/web3j/main/java");

        final File generatedContract = new File(web3jContractsDir,
                "org/web3j/test/StandardToken.java");

        assertTrue(generatedContract.exists());

        final File excludedContract = new File(web3jContractsDir,
                "org/web3j/test/Token.java");

        assertFalse(excludedContract.exists());

        final BuildResult upToDate = gradleRunner.build();
        assertNotNull(upToDate.task(":generateContractWrappers"));
        assertEquals(UP_TO_DATE, upToDate.task(":generateContractWrappers").getOutcome());
    }

}
