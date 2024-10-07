package com.example.barcode_decode

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.barcode_decode.model.Product
import com.google.mlkit.vision.barcode.BarcodeScanning
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import okhttp3.Response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.Executors
import android.Manifest
import android.os.CountDownTimer
import android.view.View


class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }

    lateinit var scanBarcodeButton: Button
    lateinit var responseTextView: TextView
    lateinit var previewView: PreviewView
    private var isScanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanBarcodeButton = findViewById(R.id.scanBarcodeButton)
        responseTextView = findViewById(R.id.responseTextView)
        previewView = findViewById(R.id.previewView)

        // Set the PreviewView visibility to GONE initially
        previewView.visibility = View.GONE

        scanBarcodeButton.setOnClickListener {
            // Check and request camera permissions
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            } else {
                // Start the camera if permission is granted
                startCamera()
            }
        }
    }

    private fun startCamera() {
        previewView.visibility = View.VISIBLE
        isScanning = true

        // Start a countdown timer for 3 seconds
        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // You can update UI here if needed
            }

            override fun onFinish() {
                // After 3 seconds, we can scan the barcode
                scanBarcode()
            }
        }.start()
    }

    @OptIn(ExperimentalGetImage::class)
    private fun scanBarcode() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Set up preview use case
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Set up barcode scanner
            val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient()

            val imageAnalysis = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor(), { imageProxy ->
                        if (!isScanning) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            barcodeScanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        val barcodeValue = barcode.rawValue
                                        barcodeValue?.let { value ->
                                            // Stop scanning once we have a barcode value
                                            isScanning = false
                                            stopCamera()
                                            sendBarcodeToApi(value)
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Failed to scan barcode.", Toast.LENGTH_SHORT).show()
                                }
                                .addOnCompleteListener {
                                    imageProxy.close() // Close imageProxy after analysis
                                }
                        }
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()  // Unbind previous use cases
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun stopCamera() {
        previewView.visibility = View.GONE // Hide the preview view
        // Any additional cleanup if necessary
    }

    private fun sendBarcodeToApi(barcodeNumber: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(RetrofitService::class.java)

        val call = apiService.getProduct(barcodeNumber)
        call.enqueue(object : Callback<Product> {
            override fun onResponse(call: Call<Product>, response: retrofit2.Response<Product>) {
                if (response.isSuccessful && response.body() != null) {
                    responseTextView.text = response.body().toString()
                } else {
                    responseTextView.text = "No product details found"
                }
            }

            override fun onFailure(call: Call<Product>, t: Throwable) {
                responseTextView.text = "Failed to fetch product details"
            }
        })
    }
}