package com.example.wheather_app_open_weather_api

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import android.Manifest


class MainActivity : AppCompatActivity() {
    // Ваш ключ API для доступа к OpenWeatherMap
    var api_key = "db435b3925bddf0584929aa6c7c666a3"

    // Определяем переменные для интерфейса пользователя и клиента местоположения
    private lateinit var btVar1: Button
    private lateinit var textView: TextView
    private lateinit var humidityTextView : TextView
    //FusedLocationProviderClient — это высокоуровневый API для отслеживания местоположения, предоставляемый Google Play Services.
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Код запроса разрешения на доступ к местоположению
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textView = findViewById(R.id.textView)
        btVar1 = findViewById(R.id.btVar1)

        humidityTextView = findViewById(R.id.humidityTextView)

      // Создаем экземпляр клиента для получения местоположения
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        // Проверяем разрешения на доступ к местоположению
        btVar1.setOnClickListener {
            checkForPermission()
        }
    }

    //Метод для проверки наличия разрешений
    private fun checkForPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Запрашиваем разрешения, если они не предоставлены
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            //  Если разрешения уже предоставлены, получаем местоположение
            obtainLocation()
        }
    }

    // Метод, который обрабатывает результат запроса разрешений
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Проверяем код запроса разрешений
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Если разрешение было предоставлено
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Получаем местоположение
                obtainLocation()
            } else {
                // Если разрешение было отклонено, показываем сообщение
                Toast.makeText(this, "Разрешение отклонено", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun obtainLocation() {
        // Получаем последнее известное местоположение
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Проверяем, что местоположение не равно null
                if (location != null) {
                    // Формируем URL для запроса к API погоды с текущими координатами
                    val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=${location.latitude}&lon=${location.longitude}&units=metric&appid=${api_key}"
                    // Запрашиваем температуру по текущему местоположению
                    getTemp(weatherUrl)
                } else {
                    // Если не удалось получить местоположение, выводим сообщение
                    Toast.makeText(this, "Не удалось получить местоположение", Toast.LENGTH_SHORT).show()
                }
            }
            //это метод, который используется в Android для обработки неудачных результатов асинхронных операций
            .addOnFailureListener { exception ->
                // Если не удалось получить местоположение, показываем сообщение
                Toast.makeText(this, "Location Permission not granted", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getTemp(url: String) {
        // Создаем очередь для запросов
        //Volley - это HTTP-библиотека, которая используется для
        // кэширования и выполнения сетевого запроса в приложениях Android.
        val queue = Volley.newRequestQueue(this)

   // Выполняем запрос к API погоды
        val stringReq = StringRequest(
            Request.Method.GET, url, { response ->
                // Получаем JSON-объект из ответа
                val obj = JSONObject(response)

                // Получаем данные о температуре из объекта
                val main: JSONObject = obj.getJSONObject("main")
                val temperature = main.getString("temp")
                println(temperature)

                // Получаем данные о влажности из объекта
                val humidity = main.getString("humidity") // Получаем влажность
                println(humidity)

                // Получаем название города из объекта
                val city = obj.getString("name")
                println(city)

                // Устанавливаем текст в textView, показывая температуру и название города
                textView.text = "${temperature} Градусов по цельсию в ${city}"
                System.out.println(obj.toString())

                // Устанавливаем влажность в humidityTextView
                humidityTextView.text = "Влажность: $humidity%"
            },
            //  В случае ошибки показываем сообщение
            { textView.text = "Ошибка!" })

        // Добавляем запрос в очередь
        queue.add(stringReq)
    }
}