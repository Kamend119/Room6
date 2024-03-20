package com.example.room6

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "foods")
class Foods(
    var name: String,
    var ccal: Int
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Entity(tableName = "users")
class Users(
    var limitccal: Float = 2000f
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 1
}

@Entity(tableName = "userfood",
    foreignKeys = [
        ForeignKey(entity = Foods::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("idfoods"),
            onDelete = ForeignKey.CASCADE ),
        ForeignKey(entity = Users::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("idusers"),
            onDelete = ForeignKey.CASCADE )
])
class UserFood(
    var idfoods: Int,
    var idusers: Int,
    var datetime: String,
    var weight: Int
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

data class FoodConsumption(
    val datetime: String,
    val name: String,
    val ccal: Int,
    val limits: Float
)

data class History(
    val limitccal: Float,
    val datetime: String,
    val summ: Int
)

@Dao
interface FoodsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFood(foods: Foods)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserFood(userFood: UserFood)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: Users)

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Int): Users?

    @Query("UPDATE users SET limitccal = :lim WHERE id = 1")
    fun updateUsers(lim: Float)

    @Query("SELECT * FROM foods")
    fun getAllFoods(): List<Foods>

    @Query("SELECT userfood.datetime, foods.name, " +
            "(foods.ccal * userfood.weight / 100) as ccal, " +
            "users.limitccal as limits " +
            "FROM userfood " +
            "INNER JOIN foods ON userfood.idfoods = foods.id " +
            "INNER JOIN users ON userfood.idusers = users.id " +
            "WHERE userfood.datetime like :date || '%'")
    fun getFoodConsumptionForDate(date: String): List<FoodConsumption>

    @Query("SELECT users.limitccal, userfood.datetime, sum(foods.ccal * userfood.weight / 100) as summ " +
            "FROM userfood " +
            "INNER JOIN foods ON userfood.idfoods = foods.id " +
            "INNER JOIN users ON userfood.idusers = users.id " +
            "GROUP BY users.limitccal, userfood.datetime")
    fun getCalorieLimitAndSumPerDay(): List<History>

}

@Database(
    entities = [Foods::class, Users::class, UserFood::class],
    version = 1
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun foodsDao(): FoodsDao
}
