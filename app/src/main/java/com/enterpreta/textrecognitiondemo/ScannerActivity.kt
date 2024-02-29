package com.enterpreta.textrecognitiondemo

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {

    private lateinit var cameraSelector: CameraSelector
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraPreview: Preview

    private lateinit var imageAnalysis: ImageAnalysis

    private lateinit var preview_view: PreviewView

    @androidx.camera.core.ExperimentalGetImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        preview_view = findViewById(R.id.preview_view)

        cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                processCameraProvider = cameraProviderFuture.get()
                bindCameraPreview()

                imageAnalysis = ImageAnalysis.Builder()
                    .setTargetRotation(preview_view.display.rotation)
                    .build()
                bindTextRecognizer()

            }, ContextCompat.getMainExecutor(this)
        )
    }

    private fun bindCameraPreview(){
        cameraPreview= Preview.Builder()
            .setTargetRotation(preview_view.display.rotation)
            .build()
        cameraPreview.setSurfaceProvider(preview_view.surfaceProvider)
        processCameraProvider.bindToLifecycle(this,cameraSelector,cameraPreview)
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun bindTextRecognizer(){
        // When using Latin script library
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val cameraExecutor = Executors.newSingleThreadExecutor()
        processCameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            val image = imageProxy.image!! // Get the image from the ImageProxy.
            val rotationDegrees =
                imageProxy.imageInfo.rotationDegrees // Get the rotation degrees from the ImageProxy.
            val inputImage = InputImage.fromMediaImage(
                image,
                rotationDegrees
            ) // Create an InputImage object with the image and rotation degrees

            recognizer.process(inputImage)
                .addOnSuccessListener{visionText->
                    val resultText = visionText.text
                    val numBlocks = visionText.textBlocks.size
                    Toast.makeText(this,resultText,Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener{
                    Toast.makeText(this, "Nothing detected", Toast.LENGTH_LONG)
                    // Task failed with an exception
                    // ...
                }
                .addOnCompleteListener{
                    imageProxy.close()
                }


        }

    }

    companion object{
        fun startScanner(context: Context, onScan: ()->Unit){
            Intent(context, ScannerActivity::class.java).also{
                context.startActivity(it)
            }
        }
    }
}