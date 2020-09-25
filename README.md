Web3j Gradle Plugin
===================

Gradle plugin that generates [Web3j][web3j] Java wrappers from Solidity smart contracts.
It smoothly integrates with your project's build lifecycle by adding specific tasks that can be also
run independently.

## Plugin configuration

To configure the Web3j Gradle Plugin using the plugins DSL or the legacy plugin application, 
check the [plugin page](https://plugins.gradle.org/plugin/org.web3j). 
The minimum Gradle version to run the plugin is `5.+`.

Then run your project containing Solidity contracts:

```
./gradlew build
```

After applying the plugin, the base directory for generated code (by default 
`$buildDir/generated/source/web3j`) will contain a directory for each source set 
(by default `main` and `test`) containing the smart contract wrappers Java classes.

### Project dependencies

The plugin requires the core [Web3j][web3j] dependency to be declared in your project.
The minimum version is 4.0 but is recommended to use the 
[latest available release](https://github.com/web3j/web3j/releases).

```groovy
dependencies {
    implementation "org.web3j:core:$web3jVersion"
}
```

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

|  Name                   | Type       | Default value                       | Description |
|-------------------------|:----------:|:-----------------------------------:|-------------|
| `generatedPackageName`  | `String`   | `${group}.web3j` or `org.web3j.{0}` | Generated contract wrappers package. |
| `generatedFilesBaseDir` | `String`   | `$buildDir/generated/source/web3j`  | Generated Java code output directory. |
| `excludedContracts`     | `String[]` | `[]`                                | Excluded contract names from wrapper generation. |
| `includedContracts`     | `String[]` | `[]`                                | Included contract names from wrapper generation. Has preference over `excludedContracts`. |
| `useNativeJavaTypes`    | `Boolean`  | `true`                              | Generate smart contract wrappers using native Java types. |
| `addressBitLength`      | `int`      | `160`                               | Supported address length in bits, by default Ethereum addresses. |

The `generatedPackageName` is evaluated as a [message format](https://docs.oracle.com/javase/6/docs/api/index.html?java/text/MessageFormat.html) 
string accepting a single parameter between curly brackets (`{0}`),
allowing to format the generated value using the contract name. For convenience,
when applied to a Java package name it will be converted to lower case. 

For instance, a `generatedPackageName` set to `${group}.{0}` in a project with group 
`com.mycompany`, a Solidity contract named `MyToken.sol` will be generated in the package
`com.mycompany.mytoken`.

Also, the default value contains the `${group}` property, which corresponds to your project artifact 
group (e.g. `com.mycompany`). If the project does not define a `group` property, the generated package
name will be `org.web3j.{0}`.

Note that message format parameters are not Gradle properties and should not be preceded by `$`.

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
source set, and a `generate[SourceSet]ContractWrappers` for each remaining source set (e.g. `test`). 

To obtain a list and description of all added tasks, run the command:

```
./gradlew tasks --all
```

[web3j]: https://web3j.io/