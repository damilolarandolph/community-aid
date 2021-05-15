package com.prog.communityaid.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.prog.communityaid.R
import com.prog.communityaid.auth.Auth
import com.prog.communityaid.data.models.Complaint
import com.prog.communityaid.data.repositories.ComplaintRepository
import com.prog.communityaid.storage.StorageController
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CreateComplaint : AppCompatActivity() {
    private lateinit var complaint: Complaint
    private var complaintRepository = ComplaintRepository()
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_VIDEO_CAPTURE = 2
    lateinit var imageView: ImageView
    lateinit var locationManager: LocationManager
    private var storageController = StorageController()
    lateinit var videoView: VideoView
    lateinit var imageFile: File
    lateinit var videoFile: File
    lateinit var loader: ConstraintLayout

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        complaint = Complaint()
        setContentView(R.layout.activity_post)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val adapter = TabAdapter(complaint, tabLayout.tabCount, supportFragmentManager, lifecycle)
        val takePictureButton = findViewById<Button>(R.id.takePicture)
        val takeVideoButton = findViewById<Button>(R.id.videoButton)
        val submitButton = findViewById<Button>(R.id.submitButton)
        imageView = findViewById(R.id.imagePreview)
        videoView = findViewById(R.id.videoView2)
        loader = findViewById(R.id.loader)
        takeVideoButton.setOnClickListener {
            Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { intent ->

                val videoFile: File? = try {
                    createVideoFile()
                } catch (ex: IOException) {
                    Toast.makeText(this, "Failed to add video", Toast.LENGTH_LONG).show()
                    null
                }

                videoFile?.also {
                    val videoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    Toast.makeText(this, videoURI.toString(), Toast.LENGTH_LONG).show()
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI)
                    this.startActivityForResult(intent, REQUEST_VIDEO_CAPTURE)

                }
            }
        }

        submitButton.setOnClickListener {
            submitComplaint()
        }
        findViewById<EditText>(R.id.postHeader).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                complaint.title = p0!!.toString()
            }

        })

        findViewById<EditText>(R.id.postDescription).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                complaint.description = p0!!.toString()
            }
        })

        takePictureButton.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
                intent.resolveActivity(packageManager)?.also {
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        Toast.makeText(this, "Failed to add Image", Toast.LENGTH_LONG).show()
                        null
                    }

                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "com.example.android.fileprovider",
                            it
                        )
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        this.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                    }

                }
            }

        }

        viewPager.adapter = adapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        })

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = locationManager.isLocationEnabled

        if (!isEnabled) {
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(settingsIntent)
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                10
            )
            return
        } else {

            val provider = locationManager.getProvider(LocationManager.GPS_PROVIDER)
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0.0F
            ) { p0 ->
                complaint.long = p0.longitude
                complaint.lat = p0.latitude
                Log.e("LOC", p0.longitude.toString())
            }
        }


    }

    private fun submitComplaint() {
        GlobalScope.launch {
            runOnUiThread {
                loader.visibility = View.VISIBLE
            }
            complaint.userId = Auth.user.id
            val pictureUrl = StorageController.upload(imageFile).await()
            complaint.picture = pictureUrl
            val videoUri = StorageController.upload(videoFile).await()
            complaint.video = videoUri
            complaintRepository.saveAsync(complaint).await()
            runOnUiThread {
                loader.visibility = View.GONE
                Snackbar.make(
                    this@CreateComplaint,
                    videoView,
                    "Complaint Created Successfully",
                    Snackbar.LENGTH_LONG
                )
                    .show()
                startActivity(Intent(this@CreateComplaint, Feed::class.java))
            }
        }

    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            imageFile = this
        }
    }

    @Throws(IOException::class)
    private fun createVideoFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_MOVIES)!!
        return File.createTempFile(
            "MP4_${timeStamp}_", /* prefix */
            ".mp4", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            videoFile = this
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data == null) {
                imageView.setImageURI(imageFile.toUri())
            }
        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            videoView.setVideoURI(videoFile.toUri())

            videoView.setOnPreparedListener { mp -> mp.isLooping = true }
            videoView.start()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val provider = locationManager.getProvider(LocationManager.GPS_PROVIDER)
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0L,
            0.0F
        ) { p0 ->
            complaint.long = p0.longitude
            complaint.lat = p0.latitude
            Log.e("LOC", p0.longitude.toString())
        }
    }
}

class TabAdapter(
    _complaint: Complaint,
    _itemCount: Int,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) :
    androidx.viewpager2.adapter.FragmentStateAdapter(fragmentManager, lifecycle) {

    private var complaint = _complaint
    private var itemCount = _itemCount
    override fun getItemCount(): Int {
        return itemCount
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                complaint.complaintType = "ecg"
                ECGFragment(complaint)
            }
            else -> {
                complaint.complaintType = "gwc"
                GWCFragment(complaint)
            }
        }
    }

}

class ECGFragment(_complaint: Complaint) : Fragment() {
    var complaint = _complaint
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_ecg, container, false)
        view.findViewById<EditText>(R.id.meterNumber).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                complaint.complaintInfo["meterNumber"] = p0!!.toString()
            }

        })
        view.findViewById<EditText>(R.id.poleNumber).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                complaint.complaintInfo["poleNumber"] = p0!!.toString()
            }

        })
        return view
    }

}

class GWCFragment(_complaint: Complaint) : Fragment() {
    private var complaint = _complaint
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_gwc, container, false)
        view.findViewById<EditText>(R.id.pumpNumber).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                complaint.complaintInfo["pumpNumber"] = p0!!.toString()
            }

        })
        return view
    }

}


