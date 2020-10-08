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

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SolidityParser(solidityFilePath: String) {

    private val sourceFile: Path = Paths.get(solidityFilePath)
    private val fileContent: String
    val libraries = mutableListOf<String>()

    init {
        if (!sourceFile.toFile().exists()) {
            throw Exception("Unable to find file: $sourceFile")
        }
        fileContent = String(Files.readAllBytes(sourceFile))
        fileContent.split("\n").forEach { line ->
            if (line.contains(Regex("import .*@openzeppelin.*"))) {
                libraries.add(line)
            }
        }
    }


    fun getImportPathsAsString(): List<String> {
        val pathList = mutableListOf<String>()
        libraries.forEach { s ->
            pathList.add(s.substring(8, s.lastIndexOf('"')))
        }
        return pathList
    }

    fun getImportNames(): List<String> {
        val name = mutableListOf<String>()

        libraries.forEach { s ->
            if (s.contains("@openzeppelin")) {
                name.add(s.substring(s.lastIndexOf('/') + 1,s.lastIndexOf('"')))
            }
        }
        return name

    }



}
