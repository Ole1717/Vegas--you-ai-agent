package com.agent.app.memory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long?,
    val request: String,
    val status: String,
    val result: String?,
    val createdAt: Long = System.currentTimeMillis()
)
