package com.agent.app.memory

import android.content.Context

class MemoryManager(context: Context) {

    private val dao =
        VegasDatabase.getInstance(context).memoryDao()

    suspend fun rememberMessage(
        role: String,
        content: String
    ) {
        dao.saveConversation(
            ConversationEntity(
                role = role,
                content = content
            )
        )
    }

    suspend fun getConversationContext(
        limit: Int = 20
    ): List<ConversationEntity> {
        return dao.getRecentConversations(limit)
            .reversed()
    }

    suspend fun createProject(
        name: String,
        repository: String? = null,
        description: String? = null
    ): Long {
        return dao.saveProject(
            ProjectEntity(
                name = name,
                repository = repository,
                description = description
            )
        )
    }

    suspend fun getProjects(): List<ProjectEntity> {
        return dao.getProjects()
    }

    suspend fun createTask(
        projectId: Long?,
        request: String
    ): Long {
        return dao.saveTask(
            TaskEntity(
                projectId = projectId,
                request = request,
                status = "running",
                result = null
            )
        )
    }

    suspend fun getRecentTasks(
        limit: Int = 20
    ): List<TaskEntity> {
        return dao.getRecentTasks(limit)
    }

    suspend fun rememberBuildError(
        projectId: Long?,
        error: String,
        solution: String? = null
    ): Long {
        return dao.saveBuildError(
            BuildErrorEntity(
                projectId = projectId,
                error = error,
                solution = solution,
                resolved = false
            )
        )
    }

    suspend fun getUnresolvedErrors():
        List<BuildErrorEntity> {
        return dao.getUnresolvedErrors()
    }
}
