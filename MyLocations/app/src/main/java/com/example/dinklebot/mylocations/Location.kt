package com.example.dinklebot.mylocations

/* ********************************* */
/* Program: My Locations */
/* Description: Class Location allows the user to enter information about a given location*/
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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_location.*

class Location : AppCompatActivity(), AdapterView.OnItemSelectedListener  {
    //global args
    private var locationType: String? = null
    private lateinit var username: String
    private lateinit var lat: String
    private lateinit var long: String
    private lateinit var myDialog: Dialog
    private var HELP_MESSAGE = "Enter Information, click the star or half star rating you would like to give\n" +
            "Select location type from drop down menu and click enter when done"

    //method to create activity but also sets up spinner from values/locations.xml using custom spinner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        val adapter = ArrayAdapter.createFromResource(this,
                R.array.location_ary, R.layout.spinner_item)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this
        val extras: Array<String> = intent.extras.getStringArray("values")
        username = extras[0]
        lat = extras[1]
        long = extras[2]
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    //sets up actions for selecting spinner items
    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        when (pos){
            0 -> locationType = ""
            1 -> locationType = "Restaurant"
            2 -> locationType = "Business"
        }
    }

    //unused but necessary callback
    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }

    //called from enter button on activity_locations
    //checks if a spinner item has been selected and if so calls proper location editer
    //will add more locations later
    fun callNext(v: View){
        when(locationType){
            "" -> Toast.makeText(this, "Please pick a location type", Toast.LENGTH_SHORT).show()
            "Restaurant" -> editRest()
            "Business" -> editBusiness()
        }
    }

    //handles calling of restaurant editor class Rest1, retrieves information entered by user and
    //sends those to the next activity
    private fun editRest(){
        val intent = Intent(this, Rest1::class.java)
        val name = locationName.text.toString()
        val desc = locationDesc.text.toString()
        if(name == "" || desc == ""){
            Toast.makeText(this, "Please enter all information", Toast.LENGTH_SHORT).show()
        }else {
            val extras: Array<String> = arrayOf(username, lat, long,
                    locationName.text.toString(),
                    locationRating.rating.toString(),
                    locationType!!,
                    locationDesc.text.toString())
            intent.putExtra("values", extras)
            startActivity(intent)
            finish()
        }
    }

    //handles calling of next business editor which is only location images for now
    //retrieves information from user and sends to the next activity
    //may add another activity in between these later for more information about businesses
    private fun editBusiness(){
        val intent = Intent(this, LocationImages::class.java)
        val name = locationName.text.toString()
        val desc = locationDesc.text.toString()
        if(name == "" || desc == ""){
            Toast.makeText(this, "Please enter all information", Toast.LENGTH_SHORT).show()
        }else {
            val extras: Array<String> = arrayOf(username, lat, long,
                    locationName.text.toString(),
                    locationRating.rating.toString(),
                    locationType!!,
                    locationDesc.text.toString())
            intent.putExtra("values", extras)
            startActivity(intent)
            finish()
        }
    }
}
