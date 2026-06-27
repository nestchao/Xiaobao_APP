package com.xiaobaotv.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [VodContentEntity::class, WatchlistEntity::class, WatchHistoryEntity::class, SearchHistoryEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vodContentDao(): VodContentDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `watch_history` (
                        `vodId` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `pic` TEXT NOT NULL,
                        `sourceIndex` INTEGER NOT NULL,
                        `sourceName` TEXT NOT NULL,
                        `episodeIndex` INTEGER NOT NULL,
                        `episodeName` TEXT NOT NULL,
                        `positionMs` INTEGER NOT NULL,
                        `durationMs` INTEGER NOT NULL,
                        `lastWatchedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`vodId`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_watch_history_lastWatchedAt` ON `watch_history` (`lastWatchedAt` DESC)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_watchlist_addedAt` ON `watchlist` (`addedAt` DESC)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `search_history` (
                        `query` TEXT NOT NULL,
                        `searchedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`query`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_search_history_searchedAt` ON `search_history` (`searchedAt` DESC)")
            }
        }
    }
}
