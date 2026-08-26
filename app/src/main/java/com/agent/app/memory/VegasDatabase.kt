package com.agent.app.memory

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ConversationEntity::class,
        ProjectEntity::class,
        TaskEntity::class,
        BuildErrorEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VegasDatabase : RoomDatabase() {

    abstract fun memoryDao(): MemoryDao

    companion object {

        @Volatile
        private var INSTANCE: VegasDatabase? = null

        fun getInstance(
            context: Context
        ): VegasDatabase {

            return INSTANCE ?: synchronized(this) {

                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    VegasDatabase::class.java,
                    "vegas_memory.db"
                )
                    .build()
                    .also {
                        INSTANCE = it
                    }
            }
        }
    }
}
