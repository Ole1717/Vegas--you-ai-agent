package com.agent.app.memory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val repository: String?,
    val description: String?,
    val lastModified: Long = System.currentTimeMillis()
)
