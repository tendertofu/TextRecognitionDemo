package com.enterpreta.textrecognitiondemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentContainerView


class MainActivity : AppCompatActivity() {

    private val cameraPermission = android.Manifest.permission.CAMERA

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted->
        if(isGranted){
           startScanner()
        }
    }

    private lateinit var button: Button
    private lateinit var containerFragment: FragmentContainerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button= findViewById(R.id.button)
        containerFragment=findViewById(R.id.containerFragment)

        //Load openCV

        /*
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV initialization failed.")
        } else {
            Log.d(TAG, "OpenCV initialization succeeded.")
        }

         */


        button.setOnClickListener{
            requestCameraAndStartScanner()
        }
        findViewById<Button>(R.id.btnOpenFragment).also{
            it.setOnClickListener{
                //match parent in height and width
                containerFragment.layoutParams= ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT)
                val fragmentManager = supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.containerFragment,ScannerFragment())
                fragmentTransaction.commit()

            }
        }


    }

    private fun requestCameraAndStartScanner(){
        if(isPermissionGranted(cameraPermission)){
            startScanner()
        }else {
            requestCameraPermission()
        }
    }

    private fun startScanner(){
        ScannerActivity.startScanner(this){

        }
    }
    private fun requestCameraPermission() {
        when {
            shouldShowRequestPermissionRationale(cameraPermission) -> {
                cameraPermissionRequest {
                    openPermissionSetting()
                }
            }
            else -> {
                requestPermissionLauncher.launch(cameraPermission)
            }
        }
    }
}