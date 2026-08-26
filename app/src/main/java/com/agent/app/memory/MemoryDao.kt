package com.agent.app.memory

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MemoryDao {

    @Insert
    suspend fun saveConversation(
        conversation: ConversationEntity
    )

    @Query(
        "SELECT * FROM conversations " +
        "ORDER BY timestamp DESC LIMIT :limit"
    )
    suspend fun getRecentConversations(
        limit: Int
    ): List<ConversationEntity>

    @Insert
    suspend fun saveProject(
        project: ProjectEntity
    ): Long

    @Query(
        "SELECT * FROM projects " +
        "ORDER BY lastModified DESC"
    )
    suspend fun getProjects(): List<ProjectEntity>

    @Insert
    suspend fun saveTask(
        task: TaskEntity
    ): Long

    @Query(
        "SELECT * FROM tasks " +
        "ORDER BY createdAt DESC LIMIT :limit"
    )
    suspend fun getRecentTasks(
        limit: Int
    ): List<TaskEntity>

    @Insert
    suspend fun saveBuildError(
        error: BuildErrorEntity
    ): Long

    @Query(
        "SELECT * FROM build_errors " +
        "WHERE resolved = 0 " +
        "ORDER BY timestamp DESC"
    )
    suspend fun getUnresolvedErrors(): List<BuildErrorEntity>
}
