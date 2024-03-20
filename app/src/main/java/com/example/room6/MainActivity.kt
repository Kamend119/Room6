package com.example.room6

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import kotlin.math.roundToInt

@Suppress("UNUSED_EXPRESSION")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val db = Room.databaseBuilder(
                LocalContext.current,
                AppDataBase::class.java, "AppDataBase"
            ).allowMainThreadQueries().build()

            val user = db.foodsDao().getUserById(1)
            if (user == null) {
                db.foodsDao().insertUser(Users(2000f))
            }

            val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")
            val current = LocalDateTime.now().format(dateTimeFormatter)

            val navController = rememberNavController()

            Scaffold(
                topBar = {TopBar(current)},
                bottomBar = {BottomBar(navController)}
            ) {
                paddingValues -> paddingValues
                NavHost(navController = navController, startDestination = "Main") {
                    composable("Main") { MainScreen(db, current, paddingValues) }
                    composable("Add") { AddScreen(db, current, paddingValues) }
                    composable("Settings") { SettingsScreen(db, paddingValues) }
                }
            }

        }
    }
}

@Composable
fun TopBar(current: String){
    Column(
        Modifier.background(Color.Gray).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(current.substring(0,8),fontSize = 30.sp)
    }
}

@Composable
fun BottomBar(navController: NavHostController){
    Row(
        Modifier
            .background(Color.Gray)
            .height(75.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Home,"",Modifier.size(35.dp).clickable {navController.navigate("Main") })
        Icon(Icons.Default.AddCircle, "",Modifier.size(35.dp).clickable { navController.navigate("Add") })
        Icon(Icons.Default.Settings, "",Modifier.size(35.dp).clickable { navController.navigate("Settings") })
    }
}

@Suppress("NAME_SHADOWING")
@Composable
fun MainScreen(db: AppDataBase, current: String, paddingValues: PaddingValues) {
    //--------------------------------------дата--------------------------------------------------------------------------------
    val foodItems = db.foodsDao().getFoodConsumptionForDate(current.substring(0, 8))
    val limit = if (foodItems.isNotEmpty()) foodItems[0].limits.roundToInt() else 0
    var today = 0
    foodItems.forEach { foodItems ->
        today += foodItems.ccal
    }

    Column(
        Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .weight(16f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            items(foodItems.size) { index ->
                Row(
                    Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                        .border(3.dp, Color.Black)
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    //--------------------------------------исправить время------------------------------------------------------------------------------
                    Text(foodItems[index].datetime.substring(9))
                    Text(foodItems[index].name)
                    Text(foodItems[index].ccal.toString())
                }
            }
        }

        Row(
            Modifier
                .background(color = if (today < limit) Color.Green else Color.Red )
                .padding(10.dp)
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Всего: $today")
            Text("(${limit - today})")
        }
    }

}

@Composable
fun AddScreen(db: AppDataBase, current:String, paddingValues: PaddingValues){
    val ctx = LocalContext.current
    var weights by remember { mutableStateOf("") }
    var selectedFoodId by remember { mutableIntStateOf(-1) }
    val foods =  db.foodsDao().getAllFoods()

    Column(
        Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .weight(2f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Добавить еду", fontSize = 40.sp)
        }

        Column(
            Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .weight(11f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                items(foods.size) { index ->
                    Row(
                        Modifier
                            .padding(bottom = 16.dp)
                            .fillMaxWidth()
                            .background(
                                if (selectedFoodId == foods[index].id)
                                    Color.Gray
                                else Color.Unspecified
                            )
                            .border(3.dp, Color.Black)
                            .padding(8.dp)
                            .clickable { selectedFoodId = foods[index].id },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = foods[index].name)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "${foods[index].ccal} ккал/100гр")
                    }
                }
            }
        }

        Column(
            Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .weight(4f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = weights,
                onValueChange = { weights = it },
                label = { Text("Грамм") }
            )

            Button(onClick = {
                val grams = weights.toIntOrNull() ?: 0
                if (grams > 0 && selectedFoodId != -1) {
                    //--------------------------------------дата--------------------------------------------------------------------------------
                    db.foodsDao().insertUserFood(UserFood(selectedFoodId, 1, current, grams))
                    Toast.makeText(ctx, "Еда добавлена", Toast.LENGTH_SHORT).show()
                } else if (grams == 0 && selectedFoodId != -1) Toast.makeText(
                    ctx,
                    "Введите вес",
                    Toast.LENGTH_SHORT
                ).show()
                else if (grams != 0 && selectedFoodId == -1) Toast.makeText(
                    ctx,
                    "Выберите еду",
                    Toast.LENGTH_SHORT
                ).show()
            }) {
                Text("Добавить")
            }

        }
    }
}

@Composable
fun SettingsScreen(db: AppDataBase, paddingValues: PaddingValues) {
    val ctx = LocalContext.current
    var name by remember { mutableStateOf("") }
    var ccal by remember { mutableStateOf("") }
    val ccalday by remember { mutableStateOf(db.foodsDao().getCalorieLimitAndSumPerDay()) }
    var ccalsd by remember  {
        if(ccalday.isNotEmpty())
            mutableFloatStateOf(ccalday[0].limitccal)
        else
            mutableFloatStateOf(2000f)
    }

    Column(
        Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .weight(2.5f)
        ) {
            Text("Лимит каллорий в день: ${ccalsd.roundToInt()}", fontSize = 20.sp)

            Slider(
                value = ccalsd,
                valueRange = 500f..5000f,
                steps = 100,
                onValueChange = { ccalsd = it }
            )

            Button(
                onClick = {
                    db.foodsDao().updateUsers(ccalsd)
                    Toast.makeText(ctx, "Лимит изменен", Toast.LENGTH_SHORT).show()
                },
                Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Установить лимит")
            }
        }

        Column(
            Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .weight(4f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Добавить продукт", fontSize = 30.sp)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название продукта") })
            OutlinedTextField(
                value = ccal,
                onValueChange = { ccal = it },
                label = { Text("Калории") })

            Button(onClick = {
                val ccalValue = ccal.toIntOrNull() ?: 0
                if (ccalValue > 0 && name.isNotBlank()) {
                    db.foodsDao().insertFood(Foods(name, ccalValue))
                    Toast.makeText(ctx, "Еда добавлена", Toast.LENGTH_SHORT).show()
                } else Toast.makeText(ctx, "Заполните данные", Toast.LENGTH_SHORT).show()
            }
            ) {
                Text("Добавить")
            }
        }


        Column(
            Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .weight(4f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("История каллорий", fontSize = 30.sp)
            LazyRow(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
            ) {
                items(ccalday.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(5.dp)
                            .border(3.dp, Color.Black)
                            .background(if (ccalday[index].summ > ccalsd) Color.Red else Color.Green)
                            .size(100.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val date = ccalday[index].datetime.substring(0, 8)
                            Text(text = date, textAlign = TextAlign.Center, color = Color.White)
                            Text(
                                text = ccalday[index].summ.toString(),
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

}