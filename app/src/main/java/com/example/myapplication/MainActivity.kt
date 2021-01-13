package com.example.myapplication

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dao = MainDatabase.getInstance(this).categoryDao
        val owner1 = Owner(1, "A1")
        dao.insert(owner1)

        val Dog = Dog(1, 1, "B1")
        dao.insert(owner1)
        dao.insert(Dog)

        Log.d("onCreat", dao.getDogsAndOwners().toString())

        // Usage of RawDao

        // Usage of RawDao
//        val query = SimpleSQLiteQuery(
//                "SELECT * FROM Dog WHERE dogId = ? LIMIT 1 join  ", arrayOf(1))
//        val raw = dao.getRow(query)
//
//        Log.d("onCreat", raw.toString())

//     val query = SimpleSQLiteQuery(
//                "SELECT * FROM Dog iNNER JOIN  Owner     ON Dog.dogOwnerId = Owner.ownerId;")
//        val raw = dao.getRow(query)
//
        val query = SimpleSQLiteQuery(
                "SELECT * FROM Dog iNNER JOIN  Owner     ON Dog.dogOwnerId = Owner.ownerId;")
        val raw = dao.getRowCursor(query)

        Log.d("onCreat raw", raw.toString())



    }
}


@Entity
data class Dog(
        @PrimaryKey(autoGenerate = true) val dogId: Long,
        val dogOwnerId: Long,
        val name: String

)

@Entity
data class Owner(@PrimaryKey val ownerId: Long, val name: String)


@Dao
interface DogDao {

    @RawQuery
    fun getRow(query: SupportSQLiteQuery): List<DogAndOwner>

    @RawQuery
    fun getRowCursor(query: SupportSQLiteQuery): List<Cursor>
    @Transaction
    @Query("SELECT * FROM Owner")
    fun getDogsAndOwners(): List<DogAndOwner>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(dog: Dog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(dog: Owner)
}

data class DogAndOwner(
        @Embedded val owner: Owner,
        @Relation(
                parentColumn = "ownerId",
                entityColumn = "dogOwnerId"
        )
        val dog: Dog
)

@Database(
        entities = [
            Dog::class,
            Owner::class
        ], version = 1
        , exportSchema = false
)
abstract class MainDatabase : RoomDatabase() {

    // abstract val heroDao: HeroDao
    abstract val categoryDao: DogDao


    companion object {

        @Volatile
        private var INSTANCE: MainDatabase? = null

        fun getInstance(context: Context) = INSTANCE ?:
        // Multiple threads can ask for the heroDao at the same time, ensure we only initialize
        // it once by using synchronized. Only one thread may enter a synchronized block at a
        // time.
        synchronized(this) {
            createDatabase(context).also { INSTANCE = it }
        }


        private fun createDatabase(context: Context): MainDatabase {
            return Room.databaseBuilder(
                    context.applicationContext,
                    MainDatabase::class.java,
                    "roomDB"
            )

                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()

        }
    }
}

