package com.example.dinklebot.mylocations

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_display_business.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class DisplayBusiness : AppCompatActivity() {

    private lateinit var title: Array<String>
    private lateinit var myDialog: Dialog
    private var HELP_MESSAGE = "Click back to return to map"

    //create activity and retrieve extras and call fillinfo method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_business)
        title = intent.extras.getStringArray("title")
        businessName.text = title[0]
        val dbHandler = myDBHandler(this, null, null, 1)
        val values = dbHandler.getBusinessInfo(title[1], title[0], title[2], title[3])
        fillBusinessInfo(values)
    }

    //add items to the option menu dynamically
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        if(title[4] == "local") {
            menu.add(0, Menu.FIRST + 1, Menu.NONE, "Delete")
            HELP_MESSAGE = "Click back to return to map\n Click delete and confirm to delete location"
        }
        menu.add(0, R.id.action_help, Menu.NONE, "Help")
        return super.onPrepareOptionsMenu(menu)
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
                help.setPadding(10, 0, 10, 100)
                true
            }
            Menu.FIRST+1 -> {
                deleteLocation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //handle user pressing the device back button to make sure the proper code is sent back
    override fun onBackPressed() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)
        super.onBackPressed()
    }

    //function to handle the information being deleted from the database, makes sure the user actually
    //wants to delete the infomation by calling an alertdialog
    fun deleteLocation(){
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.confirm)
                .setPositiveButton(R.string.delete, DialogInterface.OnClickListener { dialog, id ->
                    val dbHandler = myDBHandler(this, null, null, 1)
                    dbHandler.removeBusiness(title.sliceArray(0..3))

                    //remove image file
                    val cw = ContextWrapper(applicationContext)
                    val directory = cw.getDir("imageDir" + title[1], Context.MODE_PRIVATE)
                    try{
                        val f = File(directory.absolutePath, title[0] + title[2] + title[3] + ".jpg")
                        f.delete()
                    }catch (e: FileNotFoundException){
                        e.printStackTrace()
                    }

                    val returnIntent = Intent()
                    returnIntent.putExtra("result", "delete")
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                })
                .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
                    dialog.dismiss()
                })
        // Create the AlertDialog object and return it
        builder.create().show()
    }

    //returns user to map when they are done/ back is clicked
    fun backToMap(v: View){
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }

    //fill in activity with information from database
    fun fillBusinessInfo(values: Array<String>){
        businessRatingBar.rating = values[0].toFloat()
        businessDesc.text = values[1]
        val lat = title[2]
        val long = title[3]

        val cw = ContextWrapper(applicationContext)
        val directory = cw.getDir("imageDir" + title[1], Context.MODE_PRIVATE)
        try{
            val f = File(directory.absolutePath, title[0] + lat + long + ".jpg")
            val b = BitmapFactory.decodeStream(FileInputStream(f))
            businessImg.setImageBitmap(b)
        }catch (e: FileNotFoundException){
            e.printStackTrace()
        }
    }
}
