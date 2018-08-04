package org.web3j.gradleplugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;

public class Web3jGradlePluginTest {

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

        final File settingsFile = testProjectDir.newFile("settings.gradle");
        final String settingsFileContent = "pluginManagement {\n" +
                "    repositories {\n" +
                "        mavenCentral()\n" +
                "        mavenLocal()\n" +
                "        maven { url 'https://dl.bintray.com/ethereum/maven/' }\n" +
                "    }\n" +
                "}\n";

        writeFile(settingsFile, settingsFileContent);
    }

    @Test
    public void generateJava() throws IOException {

        final String buildFileContent = "plugins {\n" +
                "   id 'web3j-gradle-plugin' version '0.1.0.0'\n" +
                "}\n" +
                "sourceSets {\n" +
                "   main {\n" +
                "       solidity {\n" +
                "           srcDir {" +
                "               '" + sourceDir.getAbsolutePath() + "'\n" +
                "           }\n" +
                "       }\n" +
                "   }\n" +
                "}\n" +
                "repositories {\n" +
                "   mavenCentral()\n" +
                "   mavenLocal()\n" +
                "   maven { url 'https://dl.bintray.com/ethereum/maven/' }\n" +
                "}\n";

        writeFile(buildFile, buildFileContent);

        final BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("generateMainJava")
                .forwardOutput()
                .withDebug(true)
                .build();

        assertEquals(SUCCESS, result.task(":generateMainJava").getOutcome());
    }

    private void writeFile(File destination, String content) throws IOException {
        try (final BufferedWriter output = new BufferedWriter(new FileWriter(destination))) {
            output.write(content);
        }
    }

}
