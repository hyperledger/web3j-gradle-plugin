/*
 * Copyright 2020 Web3 Labs Ltd.
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
package org.web3j.gradle.plugin.library.management

import org.junit.Test

class SolidityParserTest {

    @Test
    fun `"test that imports are picked up"`() {

        val returnList = SolidityParser(
                "/Users/alexr/Documents/dev/java/solidity-dependency-managment/web3j-gradle-plugin/src/test/resources/solidity/MyCollectible.sol")
        returnList.libraries.forEach { s -> println(s) }
    }

    @Test
    fun `"test that path is extracted"`() {

        val returnList = SolidityParser(
                "/Users/alexr/Documents/dev/java/solidity-dependency-managment/web3j-gradle-plugin/src/test/resources/solidity/MyCollectible.sol")
        returnList.getImportPathsAsString().forEach { s -> println(s) }
    }


    @ExperimentalStdlibApi
    @Test
    fun `"test that npm is installed"`() {
        val npm = NpmUtils()
        npm.isNpmInstalled()

    }

    @ExperimentalStdlibApi
    @Test
    fun `"test that library is installed"`() {
        val npm = NpmUtils()
        npm.isLibraryInstalled()

    }

    @Test
    fun `"test that contract name is extracted"`() {
        val returnList = SolidityParser(
                "/Users/alexr/Documents/dev/java/solidity-dependency-managment/web3j-gradle-plugin/src/test/resources/solidity/MyCollectible.sol")
        returnList.getImportNames().forEach { s -> println(s) }
    }

    @Test
    fun `"test that contract exists in npm"`() {
        val npm = NpmUtils()

        println(npm.isImportAvailable("blablabla.sol"))
    }


}
