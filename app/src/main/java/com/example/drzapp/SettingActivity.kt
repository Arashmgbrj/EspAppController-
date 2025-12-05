package com.example.drzapp

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import android.content.Intent


class SettingActivity : AppCompatActivity() {
    private lateinit var ssidInput: EditText
    private lateinit var passwordInput: EditText


    private var fetchJob: Job? = null
    private val baseUrl = "http://192.168.4.1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting)

        ssidInput = findViewById(R.id.ssidInput)
        passwordInput = findViewById(R.id.passwordInput)


        val saveButton = findViewById<Button>(R.id.saveButton)
        val backButton = findViewById<Button>(R.id.backButton)

        saveButton.setOnClickListener {
            val ssid = ssidInput.text.toString()
            val password = passwordInput.text.toString()


            CoroutineScope(Dispatchers.Main).launch {
                sendGetRequest("$baseUrl/setWiFi?ssid=${encode(ssid)}&password=${encode(password)}")


                Toast.makeText(
                    this@SettingActivity,
                    "اطلاعات با موفقیت ارسال شد",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        startFetchingData()
    }
    private fun startFetchingData() {
        fetchJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val ssid = sendGetRequest("$baseUrl/getssid")
                val pass = sendGetRequest("$baseUrl/getpass")


                ssidInput.setText(ssid ?: "")
                passwordInput.setText(pass ?: "")


                delay(80000)
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

    private fun encode(value: String): String {
        return URLEncoder.encode(value, "UTF-8")
    }

    override fun onDestroy() {
        super.onDestroy()
        fetchJob?.cancel()
    }
}
