package com.xiaobaotv.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [VodContentEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vodContentDao(): VodContentDao
}
