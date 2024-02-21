package com.yousef.falldetection

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yousef.falldetection.databinding.ActivityMainBinding
import com.yousef.falldetection.ui.theme.FallDetectionTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity(), SensorEventListener {


    private var _binding: ActivityMainBinding? = null

    private val binding get() = _binding!!

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private val REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        sensorManager!!.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_FASTEST)

        _binding = ActivityMainBinding.inflate(layoutInflater)

        val view = binding.root

        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.HIGH_SAMPLING_RATE_SENSORS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.HIGH_SAMPLING_RATE_SENSORS)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            // Permissions have not been granted, request them
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_CODE
            )
        } else {
            // Permissions have already been granted
            sensorManager!!.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME
            )
        }

        setContentView(view)

        /*setContent {
            FallDetectionTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }*/
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            var allPermissionsGranted = true
            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }
            if (allPermissionsGranted) {
                // Permissions have been granted
                // Do something here
                sensorManager!!.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_GAME
                )
            } else {
                // Permissions have been denied
                // Handle it gracefully or request the permissions again
            }
        }
    }

    fun vibratePhone(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For newer versions (API 26+)
            val effect = VibrationEffect.createOneShot(1500, 255) // 500 ms
            vibrator.vibrate(effect)
        } else {
            // For older versions
            vibrator.vibrate(1500) // 500 ms
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {

        if(event!!.sensor.type == Sensor.TYPE_ACCELEROMETER){
            var x = event.values[0]
            var y = event.values[1]
            var z = event.values[2]

            var accelForce = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

            binding.acTv.text = accelForce.toString()

            if(accelForce<=2.1){
                //HTTP-Request
                vibratePhone(this@MainActivity)
                makeGetRequest()
            }


        }
    }

    private fun makeGetRequest() {
        val request = Request.Builder()
            .url("http://10.14.192.16:8000/")
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // Handle the error
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    // Update UI on the main thread
                    Handler(Looper.getMainLooper()).post {
                        // Handle the response, update UI
                        vibratePhone(this@MainActivity)
                    }
                }
            }
        })
    }




    /*fun sendGetRequest(urlStr: String): String {

        val url = URL(urlStr)
        val urlConnection = url.openConnection() as HttpURLConnection

        try {
            urlConnection.requestMethod = "GET"
            val inputStream = urlConnection.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuilder()
            reader.forEachLine { line ->
                response.append(line)
            }

            return response.toString()
        } finally {
            urlConnection.disconnect()
        }
    }

    fun makeRequest() {
        CoroutineScope(Dispatchers.IO).launch {
            sendGetRequest("http://192.168.0.154:5000/")
            withContext(Dispatchers.Main) {
                // Update UI with the response


            }
        }
    }*/

    override fun onDestroy() {

        super.onDestroy()
        sensorManager!!.unregisterListener(this)

    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FallDetectionTheme {
        Greeting("Android")
    }
}