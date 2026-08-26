package com.agent.app.memory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "build_errors")
data class BuildErrorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long?,
    val error: String,
    val solution: String?,
    val resolved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
