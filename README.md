web3j Gradle Plugin
===================

Simple Gradle plugin for [web3j](https://web3j.io/). This plugin is under development and not stable.

## Plugin configuration

Before you start, you will need to install the
[Solidity compiler](https://solidity.readthedocs.io/en/latest/installing-solidity.html)
if is not already installed in your computer.

### Using the `buildscript` convention

To install the web3j Plugin using the old Gradle `buildscript` convention, you should add 
the following to the first line of your build file (at the moment only release versions 
are supported in Gradle, not SNAPSHOT):

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'org.web3j:web3j-gradle-plugin:0.1.4'
    }
}

apply plugin: 'web3j'
```

### Using the plugins DSL

Alternatively, if you are using the more modern plugins DSL, add the following line to your 
build file:

```groovy
plugins {
    id 'org.web3j' version '0.1.4'
}
```

You will need to add the following configuration in the first line of your `settings.gradle` 
file to resolve the artifact from the Epiphyte repository.

```groovy
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
```

Then run this command from your project containing Solidity contracts:

```
./gradlew build
```

After applying the plugin, the base directory for generated code (by default 
`$buildDir/generated/source/web3j`) will contain a directory for each source set 
(by default `main` and `test`) containing the smart contract wrappers Java classes.

## Code generation

The `web3j` DSL allows to configure the generated code, e.g.:

```groovy
web3j {
    generatedPackageName = 'com.mycompany.{0}'
    generatedFilesBaseDir = "$buildDir/custom/destination"
    excludedContracts = ['Ownable']
    useNativeJavaTypes = false
}
```

The properties accepted by the DSL are listed in the following table: 

|  Name                   | Type       | Default value                      | Description |
|-------------------------|:----------:|:----------------------------------:|-------------|
| `generatedPackageName`  | `String`   | `${group}.web3j`                   | Generated contract wrappers package. |
| `generatedFilesBaseDir` | `String`   | `$buildDir/generated/source/web3j` | Generated Java code output directory. |
| `excludedContracts`     | `Array`    | `String[]`                         | Excluded contract names from wrapper generation. |
| `useNativeJavaTypes`    | `Boolean`  | `true`                             | Generate smart contract wrappers using native Java types. |

The `generatedPackageName` may contain a indexed value between curly brackets (`{0}`),
allowing to format the generated value using the contract name. For convenience,
when applied to a Java package name it will be converted to lower case. 

For instance, a `generatedPackageName` set to `${group}.{0}` in a project with group 
`com.mycompany`, a Solidity contract named `MyToken.sol` will be generated in the package
`com.mycompany.mytoken`. Note that this is not a Gradle property and should not be preceded by `$`.

Also, the default value contains the `${group}` property, which corresponds to your project artifact 
group (e.g. `com.mycompany`).

## Source sets

By default, all `.sol` files in `$projectDir/src/main/solidity` will be processed by the plugin.
To specify and add different source sets, use the `sourceSets` DSL:

```groovy
sourceSets {
    main {
        solidity {
            srcDir { 
                "my/custom/path/to/solidity" 
             }
        }
    }
}
```

Check the [Solidity Plugin](https://github.com/web3j/solidity-gradle-plugin)
documentation to configure the smart contracts source code directories.

Output directories for generated smart contract wrappers Java code 
will be added to your build automatically.

## Plugin tasks

The [Java Plugin](https://docs.gradle.org/current/userguide/java_plugin.html)
adds tasks to your project build using a naming convention on a per source set basis
(i.e. `compileJava`, `compileTestJava`).

Similarly, the Solidity plugin will add the `generateContractWrappers` task for the project `main`
source set, and a `compile[SourceSet]Solidity` for each remaining source set (e.g. `test`). 

To obtain a list and description of all added tasks, run the command:

```
./gradlew tasks --all
```