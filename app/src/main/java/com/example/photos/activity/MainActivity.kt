package com.example.photos.activity

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.photos.R
import com.example.photos.adapter.GalleryImageAdapter
import com.example.photos.adapter.GalleryImageClickListener
import com.example.photos.adapter.Image
import com.example.photos.databinding.ActivityMainBinding
import com.example.photos.fragment.GalleryFullscreenFragment
import com.example.photos.fragment.OnFragmentInteractionListener
import com.example.photos.fragment.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File

class MainActivity : AppCompatActivity(), GalleryImageClickListener, OnFragmentInteractionListener {

    // Gallery column count
    private val PREF_NAME = "YourPREF_NAME"
    private val IMAGES_DIRECTORY_KEY = "ImagesDirectoryKey"
    private val SPAN_COUNT = 3
    private val imageList = ArrayList<Image>()
    private lateinit var galleryAdapter: GalleryImageAdapter
    private lateinit var navBar: BottomNavigationView

    companion object {
        var imagesDirectory = ""
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navBar = binding.bottomNavigationView
        binding.recyclerView.setScrollbarFadingEnabled(false)
        navBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    binding.recyclerView.visibility = android.view.View.VISIBLE // Show recyclerview
                    binding.fragmentContainer.visibility = android.view.View.GONE // Hide fragment container
                    true
                }

                R.id.navigation_settings -> {
                    binding.recyclerView.visibility = android.view.View.GONE // Hide recyclerview
                    binding.fragmentContainer.visibility = android.view.View.VISIBLE // Show fragment container
                    val settingsFragment = SettingsFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, settingsFragment)
                        .addToBackStack(null)
                        .commit()
                    true
                }

                else -> false
            }
        }
        initializeOnce()
        imagesDirectory = getImagesDirectory()
        getPermission { granted ->
            if (granted) {
                setupGallery()
                // Load images
                loadImages()
            } else {
                // Handle permission denied case
            }
        }
    }

    private fun getPermission(callback: (Boolean) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        } else {
            callback(true)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
            // Continue with setup and loading
            setupGallery()
            // Load images
            loadImages()
        } else {
            // Permission denied
            // Handle permission denied case
        }
    }

    private fun setupGallery() {
        galleryAdapter = GalleryImageAdapter(imageList)
        galleryAdapter.listener = this
        binding.recyclerView.layoutManager = GridLayoutManager(this, SPAN_COUNT)
        binding.recyclerView.adapter = galleryAdapter
    }

    private fun loadImages() {
        val imagePaths = scanImagePaths(File(imagesDirectory))
        for (path in imagePaths) { // Add images to list
            imageList.add(Image(path, path.substringAfterLast("/")))
            galleryAdapter.notifyItemChanged(imageList.size - 1)
        }
    }

    private fun scanImagePaths(directory: File): List<String> {
        val imagePaths = mutableListOf<String>()
        val files = directory.listFiles() ?: return imagePaths
        for (file in files) {
            if (file.isDirectory) {
                imagePaths.addAll(scanImagePaths(file))
            } else if (file.isFile && isImageFile(file)) {
                imagePaths.add(file.path)
            }
        }
        return imagePaths
    }

    private fun isImageFile(file: File): Boolean {
        val imageExtensions = listOf("jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp", "svg", "heic", "heif", "raw")
        val extension = file.extension.lowercase()
        return extension in imageExtensions
    }

    override fun onClick(position: Int) {
        // Handle click of image
        val bundle = Bundle()
        bundle.putSerializable("images", imageList)
        bundle.putInt("position", position)

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val galleryFragment = GalleryFullscreenFragment()
        galleryFragment.arguments = bundle
        galleryFragment.show(fragmentTransaction, "gallery")
    }

    override fun ResetImages() {
        imageList.clear()
        galleryAdapter.notifyDataSetChanged()
        loadImages()
    }

    // This function is called only once when the app is installed
    // for the first time and when the user changes the directory in the settings
    private fun initializeOnce() {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (!sharedPreferences.contains(IMAGES_DIRECTORY_KEY)) {
            val editor = sharedPreferences.edit()
            editor.putString(IMAGES_DIRECTORY_KEY, Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera")
            editor.apply()
        }
    }

    private fun getImagesDirectory(): String {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(IMAGES_DIRECTORY_KEY, "") ?: ""
    }

    private fun setImagesDirectory(directory: String) {
        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(IMAGES_DIRECTORY_KEY, directory)
        editor.apply()
    }

    override fun onStop() {
        super.onStop()
        setImagesDirectory(imagesDirectory) // TODO: It'd be better to check if the directory has changed; if not, don't write again
    }
}
