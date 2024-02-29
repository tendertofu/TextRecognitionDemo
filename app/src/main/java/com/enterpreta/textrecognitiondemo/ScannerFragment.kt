package com.enterpreta.textrecognitiondemo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

//import kotlinx.android.synthetic.main.fragment_scanner.view.*  DEPRECATED
import java.util.concurrent.Executors

private lateinit var cameraSelector: CameraSelector
private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
private lateinit var processCameraProvider: ProcessCameraProvider
private lateinit var cameraPreview: Preview
private lateinit var imageView: ImageView

//private lateinit var imageCapture: ImageCapture

private lateinit var imageAnalysis: ImageAnalysis

private lateinit var preview_view: PreviewView
private lateinit var btnTakePhoto: Button
private lateinit var context: Context


/**
 * A simple [Fragment] subclass.
 * Use the [ScannerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScannerFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("MissingInflatedId")
    @androidx.camera.core.ExperimentalGetImage
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val viewOfLayout = inflater.inflate(R.layout.fragment_scanner, container, false)
        preview_view = viewOfLayout.findViewById(R.id.preview_view)
        btnTakePhoto = viewOfLayout.findViewById(R.id.btnTakePhoto)
        imageView = viewOfLayout.findViewById(R.id.imgView)


        cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                processCameraProvider = cameraProviderFuture.get()
                bindCameraPreview()

                /*  imageAnalysis = ImageAnalysis.Builder()
                      .setTargetRotation(preview_view.display.rotation)
                      .build()
                  bindTextRecognizer()*/

            }, ContextCompat.getMainExecutor(requireContext())
        )


        btnTakePhoto.setOnClickListener {
           val imageCapture = ImageCapture.Builder()
                .setTargetRotation(preview_view.display.rotation)
                .build()
            processCameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, cameraPreview)

            val cameraExecutor = Executors.newSingleThreadExecutor()

            imageCapture.takePicture(cameraExecutor, object: ImageCapture.OnImageCapturedCallback(){
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    //image is jpeg format
                    val formatImage = image.format
                    requireActivity().runOnUiThread(java.lang.Runnable {
                        imageView.setImageBitmap(image.image?.toBitmap())
                        imageView.layoutParams=ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT)
                        imageView.visibility=View.VISIBLE
                        image.close()
                    })


                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)

                }


            })

        }


        return viewOfLayout

    }

    private fun bindCameraPreview() {
        cameraPreview = Preview.Builder()
            .setTargetRotation(preview_view.display.rotation)
            .build()
        cameraPreview.setSurfaceProvider(preview_view.surfaceProvider)
        processCameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview)
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun bindTextRecognizer() {
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
                .addOnSuccessListener { visionText ->
                    val resultText = visionText.text
                    val numBlocks = visionText.textBlocks.size
                    Toast.makeText(requireContext(), resultText, Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Nothing detected", Toast.LENGTH_LONG)
                    // Task failed with an exception
                    // ...
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }


        }

    }

    //extension function to convert Image (JPG) to bitmap
    fun Image.toBitmap(): Bitmap {
        val buffer = planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    /*companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ScannerFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ScannerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    } */
}