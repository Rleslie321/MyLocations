package com.example.dinklebot.mylocations

/* ********************************* */
/* Program: My Locations */
/* Description: Class Rest1 allows the user to enter in menu items tried at the location*/
/* Author: Robert Leslie */
/* Notes: */
/* Reference: */
/* Last Modified: 6/8/2018 */
/* ********************************** */

import android.app.Dialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_rest1.*

class Rest1 : AppCompatActivity() {
    //global args
    private lateinit var extras: Array<String>
    private lateinit var myDialog: Dialog
    private var HELP_MESSAGE = "Enter any menu items tried, click enter or click skip"

    //creates activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rest1)
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
                help.setPadding(20, 0, 20, 100)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //called from rest1 activity skip button, starts the location images activity without adding anything
    fun skipAdding(v: View){
        val intent = Intent(this, LocationImages::class.java)
        intent.putExtra("values", extras)
        startActivity(intent)
        finish()
    }

    //called from rest1 activity enter button, takes in the given information from the user
    //as long as they add something, then calls the location images activity giving the information
    //it has gotten
    fun addImages(v: View){
        val notEntered = booleanArrayOf(true, true, true, true)
        var appName = ""
        var entreeName = ""
        var drinkName = ""
        var dessertName = ""
        if(appetizer.text.toString() != ""){ appName = appetizer.text.toString(); notEntered[0] = false}
        if(entree.text.toString() != ""){ entreeName = entree.text.toString(); notEntered[1] = false}
        if(drink.text.toString() != ""){ drinkName = drink.text.toString(); notEntered[2] = false}
        if(dessert.text.toString() != ""){ dessertName = dessert.text.toString(); notEntered[3] = false}
        if(notEntered.contains(false)) {
            extras += arrayOf(appName, entreeName, drinkName, dessertName)
            val intent = Intent(this, LocationImages::class.java)
            intent.putExtra("values", extras)
            startActivity(intent)
            finish()
        }else{
            Toast.makeText(this, "Please enter one value or press skip", Toast.LENGTH_SHORT).show()
        }
    }
}
