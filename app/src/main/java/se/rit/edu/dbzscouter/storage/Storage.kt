package se.rit.edu.dbzscouter.storage

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.content.Context


@Entity(tableName = "readings")
data class Reading(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @ColumnInfo(name = "name") var name: String = "",
    @ColumnInfo(name = "power") var powerLevel: Int = 0
) {
    @Ignore
    constructor(): this(0, "", 0)
}

@Dao
interface ReadingDao {
    @Query("SELECT * FROM readings")
    fun all(): List<Reading>

    @Query("SELECT * FROM readings WHERE name LIKE :name")
    fun allWithName(name: String): List<Reading>

    @Insert(onConflict = REPLACE)
    fun insert(reading: Reading)

    @Delete
    fun delete(reading: Reading)
}

@Database(entities = [Reading::class], version = 1, exportSchema = false)
abstract class ReadingDatabase : RoomDatabase() {
    abstract fun readingDao(): ReadingDao

    companion object {
        private var INSTANCE: ReadingDatabase? = null

        fun getInstance(context: Context): ReadingDatabase {
            val inst = INSTANCE
            if (inst == null) {
                synchronized(ReadingDatabase::class) {
                    val newInst = Room.databaseBuilder(context.applicationContext,
                            ReadingDatabase::class.java, "readings.db").build()
                    INSTANCE = newInst
                    return newInst
                }
            } else {
                return inst
            }
        }
    }
}