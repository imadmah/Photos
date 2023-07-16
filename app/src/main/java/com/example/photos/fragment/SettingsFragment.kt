package com.example.photos.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.photos.R
import com.example.photos.activity.MainActivity
import java.net.URLDecoder

class SettingsFragment : PreferenceFragmentCompat() {
    private val REQUEST_CODE = 0 // Some integer for the callback
    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // IMPLEMENT THE BUTTON TO CHANGE THE FOLDER WHICH ITS IMAGES WILL BE SHOWN IN THE APP //
        val button = preferenceManager.findPreference<Preference>("ImagesFolder")
        button?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            startActivityForResult(intent, REQUEST_CODE)
            true
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data // Get the selected folder URI
            // Handle the selected folder URI here
            val decodedUriString = URLDecoder.decode(uri.toString(), "UTF-8")
            MainActivity.imagesDirectory= getFolderPathFromUri(decodedUriString) // Set the imagesDirectory to the selected folder


            mListener = try {
                context as OnFragmentInteractionListener
            }
            catch (e: ClassCastException) {
                throw ClassCastException(context.toString() + " must implement OnFragmentInteractionListener")
            }
            mListener?.ResetImages()


        }

    }

    private fun getFolderPathFromUri(uri: String): String {
        return createRelativePath(extractWordsAfter(uri))
            .let { "/storage/emulated/0/$it" }
    }

    private fun extractWordsAfter(input: String): List<String> {
        val startIndex = input.indexOf("primary:")
        return input.substring(startIndex + "primary:".length)
            .split("/")
            .filter { it.isNotBlank() }
    }

    private fun createRelativePath(words: List<String>): String {
        val pathSeparator = "/"
        return words.joinToString(separator = pathSeparator)
    }




}