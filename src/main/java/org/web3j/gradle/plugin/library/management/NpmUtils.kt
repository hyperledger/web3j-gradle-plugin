package org.web3j.gradle.plugin.library.management

import java.io.File

class NpmUtils {
    val defaultUnixNpmPath = "/usr/local/lib/node_modules/@openzeppelin/contracts"

    @ExperimentalStdlibApi
    fun isNpmInstalled(): Boolean {
        val process = ProcessBuilder("npm", "--version")
                .start().inputStream.readAllBytes().decodeToString()
        println(process)
        return false
    }

    @ExperimentalStdlibApi
    fun isLibraryInstalled(): Boolean {
        val process = ProcessBuilder("npm","list","-g", "@openzeppelin/contracts")
                .start().inputStream.readAllBytes().decodeToString()
        println(process)
        return process.isEmpty()
    }

    fun isImportAvailable(importName: String): Boolean {
        File(defaultUnixNpmPath).walk().forEach {
            if(it.name == importName){
                return true

            }
        }


        return false
    }
}