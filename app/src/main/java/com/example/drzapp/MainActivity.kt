package com.example.drzapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var lockIcon: ImageView
    private lateinit var dataTextView: TextView
    private lateinit var Sensor1: TextView
    private lateinit var Sensor2: TextView
    private lateinit var SettingBtn: ImageView
    private lateinit var lockButton: LinearLayout

    private var fetchJob: Job? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // ارجاع به ویوها
        lockIcon = findViewById(R.id.lockIcon)
        dataTextView = findViewById(R.id.temperatureText)
        Sensor1 = findViewById(R.id.sens1)
        Sensor2 = findViewById(R.id.sens2)
        SettingBtn = findViewById(R.id.settingsButton)
        lockButton = findViewById(R.id.Lock)

        lockIcon.setTag(R.drawable.lock)

        // شروع دریافت داده‌ها از سرور
        startFetchingData()

        // عملکرد دکمه تنظیمات (اگر نیاز بود فعال شود)

        SettingBtn.setOnClickListener {
            Toast.makeText(this@MainActivity, "Old src: sdszdsadsadasd", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
        val destroy = findViewById<ImageView>(R.id.backButton)
        destroy.setOnClickListener {
            finish()
        }


        // عملکرد کلیک روی قفل
        lockButton.setOnClickListener {
            val oldResId = lockIcon.tag as Int
            val oldResName = resources.getResourceEntryName(oldResId)

            CoroutineScope(Dispatchers.Main).launch {
                val currentState = sendGetRequest("http://192.168.4.1/c_sw1")

                Toast.makeText(this@MainActivity, "Old src: $oldResName", Toast.LENGTH_SHORT).show()

                if (currentState == "OFF") {
                    sendGetRequest("http://192.168.4.1/on1")
                } else {
                    sendGetRequest("http://192.168.4.1/off1")
                }
            }
        }
    }

    private fun startFetchingData() {
        fetchJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val tempData = sendGetRequest("http://192.168.4.1/getTemp")
                val lockState = sendGetRequest("http://192.168.4.1/c_sw1")
                val sensor1State = sendGetRequest("http://192.168.4.1/state_sensor1")
                val sensor2State = sendGetRequest("http://192.168.4.1/state_sensor2")

                Sensor1.text = if (sensor1State == "ok") "Online Sensor 1" else "Offline Sensor 1"
                Sensor2.text = if (sensor2State == "ok") "Online Sensor 2" else "Offline Sensor 2"

                if (lockState == "ON") {
                    lockIcon.setImageResource(R.drawable.lock)
                    lockIcon.setTag(R.drawable.lock)
                } else {
                    lockIcon.setImageResource(R.drawable.unlock)
                    lockIcon.setTag(R.drawable.unlock)
                }

                dataTextView.text = tempData ?: "خطا!"
                delay(1000)
            }
        }
    }

    private suspend fun sendGetRequest(urlString: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fetchJob?.cancel()
    }
}
