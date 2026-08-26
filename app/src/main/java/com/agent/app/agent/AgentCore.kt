package com.agent.app.agent

import com.agent.app.files.FileManager
import com.agent.app.memory.MemoryManager

data class AgentRequest(
    val message: String
)

data class AgentContext(
    val messages: List<String>,
    val projects: List<String>,
    val tasks: List<String>
)

class AgentCore(
    private val memory: MemoryManager,
    private val files: FileManager
) {

    suspend fun buildContext(
        request: AgentRequest
    ): AgentContext {

        val messages =
            memory.getConversationContext(20)
                .map {
                    "${it.role}: ${it.content}"
                }

        val projects =
            memory.getProjects()
                .map {
                    it.name
                }

        val tasks =
            memory.getRecentTasks(20)
                .map {
                    "${it.status}: ${it.request}"
                }

        return AgentContext(
            messages = messages,
            projects = projects,
            tasks = tasks
        )
    }

    suspend fun process(
        request: AgentRequest
    ): String {

        memory.rememberMessage(
            role = "user",
            content = request.message
        )

        val context =
            buildContext(request)

        val response =
            createTemporaryResponse(
                request,
                context
            )

        memory.rememberMessage(
            role = "assistant",
            content = response
        )

        return response
    }

    suspend fun createFile(
        path: String,
        content: String
    ): Boolean {
        return files.createFile(
            path,
            content
        )
    }

    suspend fun readFile(
        path: String
    ): String {
        return files.readFile(path)
    }

    suspend fun updateFile(
        path: String,
        content: String
    ) {
        files.updateFile(
            path,
            content
        )
    }

    suspend fun deleteFile(
        path: String
    ): Boolean {
        return files.delete(path)
    }

    suspend fun listFiles(
        path: String = ""
    ): List<String> {
        return files.listFiles(path)
    }

    private fun createTemporaryResponse(
        request: AgentRequest,
        context: AgentContext
    ): String {

        if (request.message.isBlank()) {
            return "Напишите задачу."
        }

        return buildString {

            append("Задача получена.\n\n")
            append("Контекст подготовлен для AI.\n\n")

            append("Память сообщений: ")
            append(context.messages.size)
            append("\n")

            append("Проектов: ")
            append(context.projects.size)
            append("\n")

            append("Задач: ")
            append(context.tasks.size)
        }
    }
}
