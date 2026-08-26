package com.agent.app.files

import java.io.File

class FileManager(
    private val rootDirectory: File
) {

    init {
        if (!rootDirectory.exists()) {
            rootDirectory.mkdirs()
        }
    }

    private fun safeFile(path: String): File {
        val root = rootDirectory.canonicalFile
        val file = File(root, path).canonicalFile

        require(
            file.path == root.path ||
            file.path.startsWith(root.path + File.separator)
        ) {
            "Недопустимый путь"
        }

        return file
    }

    fun createDirectory(path: String): Boolean {
        return safeFile(path).mkdirs()
    }

    fun createFile(path: String, content: String = ""): Boolean {
        val file = safeFile(path)

        file.parentFile?.mkdirs()

        return if (!file.exists()) {
            file.writeText(content)
            true
        } else {
            false
        }
    }

    fun readFile(path: String): String {
        return safeFile(path).readText()
    }

    fun updateFile(path: String, content: String) {
        val file = safeFile(path)

        file.parentFile?.mkdirs()
        file.writeText(content)
    }

    fun delete(path: String): Boolean {
        return safeFile(path).deleteRecursively()
    }

    fun exists(path: String): Boolean {
        return safeFile(path).exists()
    }

    fun listFiles(path: String = ""): List<String> {
        val directory = safeFile(path)

        if (!directory.exists() || !directory.isDirectory) {
            return emptyList()
        }

        return directory
            .listFiles()
            ?.map { it.relativeTo(rootDirectory).path }
            ?.sorted()
            ?: emptyList()
    }
}
