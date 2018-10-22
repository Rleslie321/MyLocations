package com.example.dinklebot.mylocations

/* ********************************* */
/* Program: My Locations */
/* Description: Class LocationImages allows the user to upload an image from their gallery*/
/* Author: Robert Leslie */
/* Notes: */
/* Reference: */
/* Last Modified: 6/8/2018 */
/* ********************************** */

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_location_images.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class LocationImages : AppCompatActivity() {
    //global args
    private lateinit var extras: Array<String>
    private var set = false
    private lateinit var myDialog: Dialog
    private var HELP_MESSAGE = "Click upload to add an image, click enter when done"

    //creates activity, also sets up button click listener for upload button to retrieve images and
    //request permissions if necessary
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_images)
        val uploadBtn = findViewById<Button>(R.id.picUpBtn)
        uploadBtn.setOnClickListener{
            val i = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            if (!checkIfAlreadyHavePermission()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
                }
            } else {
                startActivityForResult(i, 1)
            }
        }
        extras = intent.extras.getStringArray("values")
    }

    //creates the options menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    //sets up option menu item actions
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_help -> {
                myDialog = Dialog(this, R.style.PauseDialog)
                myDialog.setContentView(R.layout.help)
                myDialog.setCancelable(true)
                myDialog.show()
                val help = myDialog.findViewById<TextView>(R.id.helpText)
                help.text = HELP_MESSAGE
                help.gravity = Gravity.CENTER
                help.setPadding(0, 0, 0, 100)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //checks if the app already has persmission to access the gallery
    private fun checkIfAlreadyHavePermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }
    //gets permission from the user when necessary
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val i = Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(i, 1)

                } else {
                    Toast.makeText(this, "Please give your permission.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    //retrieves the image from the gallery and adds it to the imageview on the locationImages activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && null != data) {
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val cursor = contentResolver.query(selectedImage!!,
                    filePathColumn, null, null, null)
            cursor!!.moveToFirst()

            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()

            val imageView = findViewById<View>(R.id.locationImg) as ImageView
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath))
            set = true
        }
    }

    //called from enter button on location images activity, handles required calls to database based
    //on the information currently collected from user, makes sure an image has been added, creates
    //a file to store the image retrieved by user, and returns the user to the map
    fun finishEdit(v: View){
        if(!set){
            Toast.makeText(this, "Please add a picture", Toast.LENGTH_SHORT).show()
        }
        else {
            val dbHandler = myDBHandler(this, null, null, 1)
            if (extras.size >= 7) {
                dbHandler.addLocation(extras)
            }
            Log.i("size of this", extras.size.toString())
            if (extras.size == 11) {
                dbHandler.addMenu(extras)
            }
            val cw = ContextWrapper(applicationContext)
            val directory = cw.getDir("imageDir" + extras[0], Context.MODE_PRIVATE)
            val myPath = File(directory, extras[3] + extras[1] + extras[2] + ".jpg")
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(myPath)
                val bitmapImage = (locationImg.drawable as BitmapDrawable).bitmap
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    fos?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("username", extras[0])
            startActivity(intent)
            finish()
        }
    }
}
